import AssemblyKeys._
import com.typesafe.sbt.SbtNativePackager._
import NativePackagerKeys._

assemblySettings

name := "scala-spray-rest"

version := "1.0"

scalaVersion := "2.11.2"

jarName in assembly := "spray-cassandra.jar"

mainClass in (Compile, assembly) := Some("com.roblayton.spray.Main")

resolvers += "spray repo" at "http://repo.spray.io"

enablePlugins(JavaAppPackaging)

val sprayVersion = "1.3.1"
val cassandraDriverVersion = "3.0.0"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.3.6",
  "com.typesafe.akka" %% "akka-http-experimental" % "0.7",
  "io.spray" %% "spray-routing" % sprayVersion,
  "io.spray" %% "spray-client" % sprayVersion,
  "io.spray" %% "spray-testkit" % sprayVersion % "test",
  "org.json4s" %% "json4s-native" % "3.2.10",
  "com.typesafe.scala-logging" %% "scala-logging-slf4j" % "2.1.2",
  "ch.qos.logback" % "logback-classic" % "1.1.2",
  "org.scalatest" %% "scalatest" % "2.2.2" % "test",
  "org.mockito" % "mockito-all" % "1.9.5" % "test",
  "com.datastax.cassandra" % "cassandra-driver-core" % cassandraDriverVersion,
  "io.spray" %%  "spray-json" % "1.3.2"
)

val meta = """META.INF(.)*""".r

mergeStrategy in assembly := {
  case PathList("javax", "servlet", xs @ _*)         => MergeStrategy.first
  case PathList(ps @ _*) if ps.last endsWith ".html" => MergeStrategy.first
  case "application.conf"                            => MergeStrategy.concat
  case meta(_)                                       => MergeStrategy.discard
  case x =>
    val oldStrategy = (mergeStrategy in assembly).value
    oldStrategy(x)
}

ivyScala := ivyScala.value map { _.copy(overrideScalaVersion = true) }
