package org.whsv26.tapir
package foos.create

import foos.Foo
import foos.FooValidationAlg.FooAlreadyExists
import foos.create.CreateHandler.CreateCommand
import util.bus.{Command, CommandHandler}

import cats.data.EitherT
import cats.effect.kernel.MonadCancelThrow
import cats.syntax.functor._
import doobie.implicits._
import doobie.postgres._
import doobie.util.transactor.Transactor
import eu.timepit.refined.types.numeric.NonNegInt

class CreateHandler[F[_] : MonadCancelThrow](
  xa: Transactor[F]
) extends CommandHandler[F, CreateCommand] {
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

object CreateHandler {
  case class CreateCommand(
    id: Foo.Id,
    a: NonNegInt,
    b: Boolean
  ) extends Command[Either[FooAlreadyExists, Foo]]
}
