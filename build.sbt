ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.8"

val circeVersion = "0.14.1"
val tapirVersion = "0.20.1"
val http4sVersion = "0.23.9"
val logbackVersion = "1.2.10"
val pureConfigVersion = "0.17.1"
val doobieVersion = "0.13.4"
val slickVersion = "3.3.3"
val postgresVersion = "42.3.3"
val kafkaVersion = "2.8.0"

lazy val root = (project in file("."))
  .settings(
    name := "tapir",
    idePackagePrefix := Some("org.whsv26.tapir")
  )

libraryDependencies ++= Seq(
  "com.softwaremill.sttp.tapir" %% "tapir-core" % tapirVersion,
  "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % tapirVersion,
  "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle" % tapirVersion,
  "com.softwaremill.sttp.tapir" %% "tapir-http4s-server" % tapirVersion
)

libraryDependencies += "org.http4s" %% "http4s-blaze-server" % http4sVersion
libraryDependencies += "ch.qos.logback"  % "logback-classic" % logbackVersion
libraryDependencies += "com.github.pureconfig" %% "pureconfig" % pureConfigVersion

libraryDependencies ++= Seq(
  "com.typesafe.slick" %% "slick" % slickVersion,
  "com.typesafe.slick" %% "slick-hikaricp" % slickVersion,
  "org.postgresql" % "postgresql" % postgresVersion,
)

libraryDependencies ++= Seq(
  "io.circe" %% "circe-core" % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion
)

libraryDependencies ++= Seq(
  "org.apache.kafka" % "kafka-clients" % kafkaVersion,
  "org.apache.kafka" % "kafka-streams" % kafkaVersion,
  "org.apache.kafka" %% "kafka-streams-scala" % kafkaVersion,
)

libraryDependencies += "com.github.fd4s" %% "fs2-kafka" % "3.0.0-M4"
