package org.whsv26.tapir
package config

object Config {
  type Seconds = Int

  final case class ServerConfig(host: String, port: Int)

  final case class KafkaConfig(bootstrapServers: String)

  final case class DbConfig(url: String, user: String, password: String)

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
