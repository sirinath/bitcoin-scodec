name := "bitcoin-scodec"

organization := "io.github.yzernik"

version := "0.2.8-SNAPSHOT"

scalaVersion := "2.11.6"

licenses += ("MIT", url("http://opensource.org/licenses/MIT"))

resolvers += "Sonatype Public" at "https://oss.sonatype.org/content/groups/public/"

resolvers += "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases"

libraryDependencies ++= Seq(
  "org.scodec"                    %% "scodec-core"    % "1.7.1",
  "org.scalatest"                 %% "scalatest"      % "2.2.0"  % "test",
  "org.scalacheck"                %% "scalacheck"     % "1.11.3" % "test",
  "com.github.scala-incubator.io" %% "scala-io-core"  % "0.4.3"  % "test"
)

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")

scoverage.ScoverageSbtPlugin.instrumentSettings

org.scoverage.coveralls.CoverallsPlugin.coverallsSettings

seq(bintrayPublishSettings:_*)
