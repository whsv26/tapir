package org.whsv26.tapir
package config

import cats.effect.Sync
import cats.syntax.bifunctor._
import cats.syntax.functor._
import cats.syntax.monadError._
import eu.timepit.refined.pureconfig._
import eu.timepit.refined.types.net.PortNumber
import eu.timepit.refined.types.string.NonEmptyString
import pureconfig.ConfigSource
import pureconfig.error.ConfigReaderFailures
import pureconfig.generic.auto._

import scala.concurrent.duration.FiniteDuration

object Config {
  final case class ServerConfig(host: NonEmptyString, port: PortNumber)

  final case class KafkaConfig(bootstrapServers: NonEmptyString)

  final case class DbConfig(url: NonEmptyString, user: NonEmptyString, password: String)

  final case class JwtConfig(
    privateKey: String,
    publicKey: String,
    issuer: String,
    expiry: FiniteDuration,
  )

  final case class AppConfig(
    db: DbConfig,
    server: ServerConfig,
    kafka: KafkaConfig,
    jwt: JwtConfig,
  )

  object AppConfig {
    def read[F[_]: Sync](path: String): F[AppConfig] =
      Sync[F].delay(ConfigSource.resources(path))
        .map(_.load[AppConfig].leftMap(ConfigError))
        .rethrow
  }

  private case class ConfigError(err: ConfigReaderFailures) extends Throwable {
    override val getMessage = err.prettyPrint()
  }
}
