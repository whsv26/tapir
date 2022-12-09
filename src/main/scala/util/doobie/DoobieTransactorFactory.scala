package org.whsv26.tapir
package util.doobie

import config.Config.DbConfig

import cats.effect.Resource
import cats.effect.kernel.Async
import doobie.ExecutionContexts
import doobie.hikari.HikariTransactor
import doobie.util.transactor.Transactor

object DoobieTransactorFactory {
  def apply[F[_]: Async](conf: DbConfig): Resource[F, Transactor[F]] =
    ExecutionContexts
      .fixedThreadPool[F](100)
      .flatMap { connectionExecutionContext =>
        HikariTransactor.newHikariTransactor[F](
          driverClassName = "org.postgresql.Driver",
          url = conf.url.value,
          user = conf.user.value,
          pass = conf.password,
          connectEC = connectionExecutionContext
        )
      }
}
