package org.whsv26.tapir
package infrastructure.repository.slick

import domain.foos.{Foo, FooId, FooRepositoryAlg}
import org.whsv26.tapir.application.endpoint.foos.CreateFooEndpoint.CreateFoo
import infrastructure.repository.slick.SlickFooRepositoryAlgInterpreter.foos

import cats.effect.Async
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

  private def run[R](a: DBIOAction[R, NoStream, Nothing]): F[R] = {
    val delayed = Async[F].delay(db.run(a))
    Async[F].fromFuture(delayed)
  }

  override def findById(id: FooId): F[Option[Foo]] = {
    val query = foos.filter(_.id === id.value).result.headOption
    run(query)
  }

  override def create(id: FooId, foo: CreateFoo): F[Foo] = {
    val stmt = (foos returning foos) += Foo(id, foo.a, foo.b)
    run(stmt)
  }

  override def delete(id: FooId): F[Unit] = {
    val stmt = foos.filter(_.id === id.value).delete
    run(stmt).void
  }
}

object SlickFooRepositoryAlgInterpreter {
  class Foos(tag: Tag) extends Table[Foo](tag, "foos") {
    def id: Rep[UUID] = column[UUID]("id", O.PrimaryKey)
    def a: Rep[Int] = column[Int]("a")
    def b: Rep[Boolean] = column[Boolean]("b")

    def * : ProvenShape[Foo] = (id, a, b) <>[Foo] (
      { case (id, a, b) =>
        Foo(
          FooId(id),
          refineV[NonNegative](a).toOption.get, // TODO
          b
        )
      },
      { case Foo(id, a, b) => (id.value, a.value, b).some }
    )
  }

  val foos = TableQuery[Foos]
}
