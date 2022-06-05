package org.whsv26.tapir
package infrastructure.storage.slick

import config.Config.DbConfig

import cats.effect.{Resource, Sync}
import slick.jdbc.JdbcBackend.{Database, DatabaseDef}

object SlickDatabaseFactory {
  def apply[F[_]: Sync](conf: DbConfig): Resource[F, DatabaseDef] =
    Resource.fromAutoCloseable(Sync[F].delay(Database.forDriver(
      new org.postgresql.Driver,
      conf.url.value,
      conf.user.value,
      conf.password
    )))
}
