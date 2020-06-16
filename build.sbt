ThisBuild / version := "0.1"
ThisBuild / scalaVersion := "2.13.2"

val circeVersion = "0.13.0"

lazy val localCognito = (project in file("cognito"))
  .settings(
    name := "tdr-local-aws",
    libraryDependencies ++= Seq(
      "ch.megard" %% "akka-http-cors" % "0.4.3",
      "com.typesafe.akka" %% "akka-http" % "10.1.12",
      "com.typesafe.akka" %% "akka-stream" % "2.6.3",
      "de.heikoseeberger" %% "akka-http-circe" % "1.32.0",
      "io.circe" %% "circe-core" % circeVersion,
      "io.circe" %% "circe-generic" % circeVersion,
      "io.circe" %% "circe-parser" % circeVersion
    )
  )

lazy val backendChecks = (project in file("backend-checks"))
  .settings(
    name := "tdr-local-backend-checks"
  )