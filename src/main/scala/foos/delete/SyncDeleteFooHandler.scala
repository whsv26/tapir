package org.whsv26.tapir
package foos.delete

import foos.DeleteFooCommand
import util.bus.CommandHandler

import cats.effect.kernel.MonadCancelThrow
import cats.syntax.functor._
import doobie.implicits.toSqlInterpolator
import doobie.syntax.all._
import doobie.util.transactor.Transactor

class SyncDeleteFooHandler[F[_]: MonadCancelThrow](
  xa: Transactor[F]
) extends CommandHandler[F, DeleteFooCommand] {
  override def handle = { command =>
    sql"DELETE FROM foos WHERE id = ${command.id.toString}"
      .update
      .run
      .transact(xa)
      .void
  }
}

