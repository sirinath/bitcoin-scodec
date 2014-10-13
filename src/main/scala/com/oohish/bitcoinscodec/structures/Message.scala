package com.oohish.bitcoinscodec.structures

import scala.language.existentials
import scalaz.-\/
import scalaz.\/-
import scala.language.implicitConversions
import scodec.Codec
import scodec.codecs._
import scodec.bits._
import com.oohish.bitcoinscodec.structures._
import com.oohish.bitcoinscodec.messages._
import java.security.MessageDigest
import java.nio.ByteBuffer
import java.nio.ByteOrder
import com.oohish.bitcoinscodec.util.Util

trait Message { self =>
  type E >: self.type <: Message
  def companion: MessageCompanion[E]
  def instance: E = self
}

trait MessageCompanion[E <: Message] {
  def codec(version: Int): Codec[E]
  def command: String
}

object Message {

  def padCommand(command: String) = {
    ByteVector(command.getBytes()) ++
      ByteVector.fill(12 - command.length())(0)
  }

  implicit def codec(magic: Long, version: Int): Codec[Message] = {
    new Codec[Message] {
      def encode(msg: Message) = {
        val c = msg.companion.codec(version)
        for {
          magic <- uint32L.encode(magic)
          command <- bytes(12).encode(padCommand(msg.companion.command))
          payload <- c.encode(msg)
          length <- uint32L.encode(payload.length / 8)
          chksum <- uint32L.encode(Util.checksum(payload.toByteVector))
        } yield magic ++ command ++ length ++ chksum ++ payload

      }
      def decode(bits: BitVector) = {
        for {
          m <- uint32L.decode(bits) match {
            case \/-((rem, mg)) =>
              if (mg == magic)
                \/-((rem, mg))
              else
                -\/(("magic did not match."))
            case -\/(err) => -\/(err)
          }
          (mrem, _) = m
          c <- bytes(12).decode(mrem)
          (crem, command) = c
          cmd = MessageCompanion.byCommand(command)
          l <- uint32L.decode(crem)
          (lrem, length) = l
          ch <- uint32L.decode(lrem)
          (chrem, chksum) = ch
          (payload, rest) = chrem.splitAt(length * 8)
          res <- cmd.codec(version).decode(payload) match {
            case \/-((rem, p)) =>
              if (!rem.isEmpty)
                -\/(("payload length did not match."))
              else if (Util.checksum(payload.toByteVector) == chksum) {
                \/-((rest, p))
              } else {
                -\/(("checksum did not match."))
              }
            case -\/(err) => -\/(err)
          }
        } yield res
      }
    }
  }

}

object MessageCompanion {
  val all: Set[MessageCompanion[_ <: Message]] = Set(Addr, Alert, Block, GetAddr, GetBlocks,
    GetData, GetHeaders, Headers, Inv, MemPool, NotFound, Ping, Pong, Reject,
    Tx, Verack, Version)
  val byCommand: Map[ByteVector, MessageCompanion[_ <: Message]] = {
    require(all.map(_.command).size == all.size, "Type headers must be unique.")
    all.map { companion => Message.padCommand(companion.command) -> companion }.toMap
  }
}