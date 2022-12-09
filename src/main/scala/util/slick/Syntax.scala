package org.whsv26.tapir
package util.slick

import util.slick.Syntax.ActionSyntax

import cats.effect.Async
import cats.effect.kernel.Sync
import slick.dbio.{DBIOAction, NoStream}
import slick.jdbc.JdbcBackend.DatabaseDef
import scala.language.implicitConversions


private[slick] trait Syntax {
  implicit def actionSyntax[R](a: DBIOAction[R, NoStream, Nothing]): ActionSyntax[R] =
    new ActionSyntax[R](a)
}

private[slick] object Syntax {
  class ActionSyntax[R](val a: DBIOAction[R, NoStream, Nothing]) extends AnyVal {
    def run[F[_] : Async](db: DatabaseDef): F[R] =
      Async[F].fromFuture {
        Sync[F].delay(db.run(a))
      }
  }
}
