package com.github.yzernik.bitcoinscodec.structures

import scalaz.\/
import com.github.yzernik.bitcoinscodec.CodecSuite
import scodec.bits._
import scodec.codecs._
import NetworkAddress.codec
import scala.BigInt
import scala.Left
import scala.Right
import scala.math.BigInt.int2bigInt
import scala.math.BigInt.long2bigInt
import java.net.InetAddress
import java.net.InetSocketAddress

class NetworkAddressSpec extends CodecSuite {

  import NetworkAddress._

  "NetworkAddress codec" should {
    "roundtrip" in {
      roundtrip(NetworkAddress(1234, new InetSocketAddress(
        InetAddress.getByAddress(Array(10, 0, 0, 1).map(_.toByte)),
        8080)))
      val bytes = hex"2001 0db8 85a3 0042 1000 8a2e 0370 7334"
      roundtrip(NetworkAddress(1234, new InetSocketAddress(InetAddress.getByAddress(bytes.toArray), 8080)))
    }

    "encode" in {
      val services = 1L
      val ip = InetAddress.getByAddress(Array(10, 0, 0, 1).map(_.toByte))
      val port = 8333
      val addr = new InetSocketAddress(ip, port)

      codec.encode(NetworkAddress(services, addr)) shouldBe
        \/.right(hex"01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 FF FF 0A 00 00 01 20 8D".toBitVector)
    }

    "decode" in {
      val services = 1L
      val ip = InetAddress.getByAddress(Array(10, 0, 0, 1).map(_.toByte))
      val port = 8333
      val addr = new InetSocketAddress(ip, port)

      codec.decode(hex"01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 FF FF 0A 00 00 01 20 8D".toBitVector) shouldBe
        \/.right(BitVector.empty, NetworkAddress(services, addr))
    }

  }
}