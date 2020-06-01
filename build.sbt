name := "tdr-local-aws"

version := "0.1"

scalaVersion := "2.13.2"

val circeVersion = "0.13.0"

libraryDependencies ++= Seq(
  "ch.megard" %% "akka-http-cors" % "0.4.3",
  "com.typesafe.akka" %% "akka-http" % "10.1.12",
  "com.typesafe.akka" %% "akka-stream" % "2.6.3",
  "de.heikoseeberger" %% "akka-http-circe" % "1.32.0",
  "io.circe" %% "circe-core" % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion
)