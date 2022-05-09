ThisBuild / version := "0.1"
ThisBuild / scalaVersion := "2.13.2"

val circeVersion = "0.14.1"
val generatedGraphQlVersion = "0.0.240"
val graphqlClientVersion = "0.0.31"
val authUtilsVersion = "0.0.50"
val akkaHttpCorsVersion = "1.1.3"
val akkaHttpVersion = "10.2.9"
val akkaStreamVersion = "2.6.19"
val akkaHttpCirceVersion = "1.39.2"

lazy val localCognito = (project in file("cognito"))
  .settings(
    name := "tdr-local-aws",
    libraryDependencies ++= Seq(
      "ch.megard" %% "akka-http-cors" % akkaHttpCorsVersion,
      "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-stream" % akkaStreamVersion,
      "de.heikoseeberger" %% "akka-http-circe" % akkaHttpCirceVersion,
      "io.circe" %% "circe-core" % circeVersion,
      "io.circe" %% "circe-generic" % circeVersion,
      "io.circe" %% "circe-parser" % circeVersion,
    )
  )

lazy val localConsignmentExport = (project in file("consignment-export"))
  .settings(
    name := "tdr-local-aws",
    libraryDependencies ++= Seq(
      "ch.megard" %% "akka-http-cors" % akkaHttpCorsVersion,
      "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-stream" % akkaStreamVersion,
      "de.heikoseeberger" %% "akka-http-circe" % akkaHttpCirceVersion,
      "uk.gov.nationalarchives" %% "tdr-graphql-client" % graphqlClientVersion,
      "uk.gov.nationalarchives" %% "tdr-generated-graphql" % generatedGraphQlVersion,
      "uk.gov.nationalarchives" %% "tdr-auth-utils" % authUtilsVersion,
      "io.circe" %% "circe-core" % circeVersion,
      "io.circe" %% "circe-generic" % circeVersion,
      "io.circe" %% "circe-parser" % circeVersion,
    )
  )

lazy val backendChecks = (project in file("backend-checks"))
  .settings(
    name := "tdr-local-backend-checks",
    libraryDependencies ++= Seq(
      "com.typesafe" % "config" % "1.4.2",
      "uk.gov.nationalarchives" %% "tdr-auth-utils" % authUtilsVersion,
      "uk.gov.nationalarchives" %% "tdr-graphql-client" % graphqlClientVersion,
      "uk.gov.nationalarchives" %% "tdr-generated-graphql" % generatedGraphQlVersion,
      "org.scalatest" %% "scalatest" % "3.2.12" % Test
    )
  )
