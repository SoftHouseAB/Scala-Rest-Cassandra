resolvers ++= Seq(
  "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/",
  "Sonatype releases"  at "https://oss.sonatype.org/content/repositories/releases/"
)

    addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.9.0")

    addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.1.1")

    addSbtPlugin("io.spray" % "sbt-revolver" % "0.8.0")