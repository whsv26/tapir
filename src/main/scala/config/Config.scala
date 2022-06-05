package org.whsv26.tapir
package config

import cats.syntax.monadError._
import cats.syntax.functor._
import cats.syntax.bifunctor._
import cats.effect.{Resource, Sync}
import eu.timepit.refined.W
import eu.timepit.refined.api.Refined
import eu.timepit.refined.numeric.Greater
import eu.timepit.refined.types.net.PortNumber
import eu.timepit.refined.types.string.NonEmptyString
import pureconfig.ConfigSource
import pureconfig.error.ConfigReaderFailures
import pureconfig.generic.auto._
import eu.timepit.refined.pureconfig._

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

  object AppConfig {
    def apply[F[_]: Sync](path: String): Resource[F, AppConfig] =
      Resource.eval {
        Sync[F].delay(ConfigSource.resources(path))
          .map(_.load[AppConfig].leftMap(ConfigError))
          .rethrow
      }
  }

  private case class ConfigError(err: ConfigReaderFailures) extends Throwable {
    override val getMessage = err.prettyPrint()
  }
}
