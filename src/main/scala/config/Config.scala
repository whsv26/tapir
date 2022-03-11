package org.whsv26.tapir
package config

object Config {
  final case class ServerConfig(host: String, port: Int)

  final case class KafkaConfig(bootstrapServers: String)

  final case class DbConfig(url: String, user: String, password: String)

  final case class JwtConfig(privateKey: String, publicKey: String)

  final case class AppConfig(
    db: DbConfig,
    server: ServerConfig,
    kafka: KafkaConfig,
    jwt: JwtConfig,
  )
}
