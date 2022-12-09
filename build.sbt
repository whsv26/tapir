ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.8"

val circeVersion = "0.14.1"
val tapirVersion = "0.20.1"
val http4sVersion = "0.23.9"
val logbackVersion = "1.2.10"
val pureConfigVersion = "0.17.1"
val slickVersion = "3.3.3"
val postgresVersion = "42.3.3"
val kafkaVersion = "2.8.0"
val fs2KafkaVersion = "3.0.0-M4"
val tsecVersion = "0.4.0"
val newTypeVersion = "0.4.4"
val refinedVersion = "0.9.28"
val chimneyVersion = "0.6.1"
val doobieVersion = "1.0.0-RC1"
val shapelessVerson = "2.3.3"

lazy val root = (project in file("."))
  .settings(
    name := "tapir",
    idePackagePrefix := Some("org.whsv26.tapir"),
    scalacOptions ++= Seq(
      "-Ymacro-annotations"
    ),
  )

libraryDependencies ++= Seq(
  "com.softwaremill.sttp.tapir" %% "tapir-core" % tapirVersion,
  "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % tapirVersion,
  "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle" % tapirVersion,
  "com.softwaremill.sttp.tapir" %% "tapir-http4s-server" % tapirVersion,

  "org.http4s" %% "http4s-blaze-server" % http4sVersion,
  "ch.qos.logback"  % "logback-classic" % logbackVersion,
  "com.github.pureconfig" %% "pureconfig" % pureConfigVersion,

  "com.typesafe.slick" %% "slick" % slickVersion,
  "com.typesafe.slick" %% "slick-hikaricp" % slickVersion,
  "org.postgresql" % "postgresql" % postgresVersion,

  "io.circe" %% "circe-core" % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion,
  "io.circe" %% "circe-refined" % circeVersion,

  "org.apache.kafka" % "kafka-clients" % kafkaVersion,
  "org.apache.kafka" % "kafka-streams" % kafkaVersion,
  "org.apache.kafka" %% "kafka-streams-scala" % kafkaVersion,

  "com.github.fd4s" %% "fs2-kafka" % fs2KafkaVersion,

  "io.github.jmcardon" %% "tsec-common" % tsecVersion,
  "io.github.jmcardon" %% "tsec-password" % tsecVersion,
  "io.github.jmcardon" %% "tsec-cipher-jca" % tsecVersion,
  "io.github.jmcardon" %% "tsec-cipher-bouncy" % tsecVersion,
  "io.github.jmcardon" %% "tsec-mac" % tsecVersion,
  "io.github.jmcardon" %% "tsec-signatures" % tsecVersion,
  "io.github.jmcardon" %% "tsec-hash-jca" % tsecVersion,
  "io.github.jmcardon" %% "tsec-hash-bouncy" % tsecVersion,
  "io.github.jmcardon" %% "tsec-jwt-mac" % tsecVersion,
  "io.github.jmcardon" %% "tsec-jwt-sig" % tsecVersion,
  "io.github.jmcardon" %% "tsec-http4s" % tsecVersion,

  "io.estatico" %% "newtype" % newTypeVersion,
  "io.estatico" %% "newtype" % newTypeVersion,

  "eu.timepit" %% "refined" % refinedVersion,
  "eu.timepit" %% "refined-cats" % refinedVersion,
  "eu.timepit" %% "refined-pureconfig" % refinedVersion,

  "io.scalaland" %% "chimney" % chimneyVersion,

  "org.tpolecat" %% "doobie-core" % doobieVersion,
  "org.tpolecat" %% "doobie-hikari" % doobieVersion,
  "org.tpolecat" %% "doobie-postgres" % doobieVersion,

  "com.chuusai" %% "shapeless" % shapelessVerson,

  "org.typelevel" %% "mouse" % "1.2.1",

  "io.7mind.izumi" %% "distage-core" % "1.1.0-M11"
)
