package org.whsv26.tapir
package modules.foos.create

import modules.foos.FooValidation.FooAlreadyExists
import modules.foos.{CreateFooCommand, Foo}
import util.bus.CommandHandler

import cats.data.EitherT
import cats.effect.kernel.MonadCancelThrow
import cats.syntax.functor._
import doobie.implicits._
import doobie.postgres._
import doobie.util.transactor.Transactor

class CreateFooHandler[F[_]: MonadCancelThrow](
  xa: Transactor[F]
) extends CommandHandler[F, CreateFooCommand] {

  override def handle = { c =>
    val select =
      sql"SELECT id, a, b FROM foos WHERE id = ${c.id.value.toString}"
        .query[Foo]
        .to[List]
        .transact(xa)
        .map(_.head)

    val insert =
      sql"INSERT INTO foos (id, a, b) values (${c.id.value.toString}, ${c.a.value}, ${c.b})"
        .update
        .run
        .attemptSomeSqlState {
          // TODO check if right sqlstate
          case sqlstate.class23.UNIQUE_VIOLATION => FooAlreadyExists(c.id)
        }
        .transact(xa)

    EitherT(insert).semiflatMap(_ => select).value
  }
}
