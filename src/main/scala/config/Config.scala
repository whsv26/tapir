package org.whsv26.tapir
package config

import eu.timepit.refined.W
import eu.timepit.refined.api.Refined
import eu.timepit.refined.numeric.Interval
import eu.timepit.refined.types.net.PortNumber
import eu.timepit.refined.types.string.NonEmptyString

object Config {
  type Seconds = Int Refined Interval.Closed[W.`60`.T, W.`86400`.T]

  final case class ServerConfig(host: NonEmptyString, port: PortNumber)

  final case class KafkaConfig(bootstrapServers: NonEmptyString)

  final case class DbConfig(url: NonEmptyString, user: NonEmptyString, password: String)

  final case class JwtConfig(
    privateKey: String,
    publicKey: String,
    issuer: String,
    expiry: Seconds,
  )

  final case class AppConfig(
    db: DbConfig,
    server: ServerConfig,
    kafka: KafkaConfig,
    jwt: JwtConfig,
  )
}
