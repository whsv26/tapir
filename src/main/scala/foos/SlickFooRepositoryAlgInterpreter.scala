package org.whsv26.tapir
package foos

import foos.SlickFooRepositoryAlgInterpreter.foos

import cats.effect.Async
import cats.effect.kernel.{Resource, Sync}
import cats.implicits._
import eu.timepit.refined.numeric.NonNegative
import eu.timepit.refined.refineV
import slick.dbio.{DBIOAction, NoStream}
import slick.jdbc.JdbcBackend.DatabaseDef
import slick.jdbc.PostgresProfile.api._
import slick.lifted.ProvenShape

import java.util.UUID

class SlickFooRepositoryAlgInterpreter[F[_]: Async](
  db: DatabaseDef
) extends FooRepositoryAlg[F] {

  private def run[R](a: DBIOAction[R, NoStream, Nothing]): F[R] =
    Async[F].fromFuture {
      Sync[F].delay(db.run(a))
    }

  override def findById(id: Foo.Id): F[Option[Foo]] = {
    val query = foos.filter(_.id === id.value).result.headOption
    run(query)
  }

  override def create(foo: Foo): F[Int] = {
    val stmt = foos += foo
    run(stmt)
  }

  override def update(foo: Foo): F[Int] = {
    val stmt = foos.filter(_.id === foo.id.value).update(foo)
    run(stmt)
  }

  override def delete(id: Foo.Id): F[Unit] = {
    val stmt = foos.filter(_.id === id.value).delete
    run(stmt).void
  }
}

object SlickFooRepositoryAlgInterpreter {
  def apply[F[_]: Async](
    db: DatabaseDef
  ): Resource[F, SlickFooRepositoryAlgInterpreter[F]] =
    Resource.suspend(Sync.Type.Delay) {
      new SlickFooRepositoryAlgInterpreter(db)
    }


  class Foos(tag: Tag) extends Table[Foo](tag, "foos") {
    def id: Rep[UUID] = column[UUID]("id", O.PrimaryKey)
    def a: Rep[Int] = column[Int]("a")
    def b: Rep[Boolean] = column[Boolean]("b")

    def * : ProvenShape[Foo] = (id, a, b) <>[Foo] (
      { case (id, a, b) =>
        Foo(
          Foo.Id(id),
          refineV[NonNegative](a).toOption.get, // TODO
          b
        )
      },
      { case Foo(id, a, b) => (id.value, a.value, b).some }
    )
  }

  val foos = TableQuery[Foos]
}
