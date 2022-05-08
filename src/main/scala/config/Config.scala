package org.whsv26.tapir
package config

import eu.timepit.refined.W
import eu.timepit.refined.api.Refined
import eu.timepit.refined.numeric.Greater
import eu.timepit.refined.types.net.PortNumber
import eu.timepit.refined.types.string.NonEmptyString

object Config {
  type Seconds = Int Refined Greater[W.`60`.T]

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
