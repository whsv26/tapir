package org.whsv26.tapir

import Foo.FooId
import SlickFooRepositoryInterpreter.foos
import cats.effect.Async
import slick.dbio.{DBIOAction, NoStream}
import slick.jdbc.JdbcBackend.DatabaseDef
import slick.jdbc.PostgresProfile.api._
import slick.lifted.ProvenShape

class SlickFooRepositoryInterpreter[F[_]: Async](
  db: DatabaseDef
) extends FooRepositoryAlgebra[F] {

  private def run[R](a: DBIOAction[R, NoStream, Nothing]): F[R] = {
    val delayed = Async[F].delay(db.run(a))
    Async[F].fromFuture(delayed)
  }

  override def findById(id: FooId): F[Option[Foo]] = {
    val query = foos.filter(_.id === id).result.headOption
    run(query)
  }

  override def create(foo: Foo): F[FooId] = {
    val stmt = (foos returning foos.map(_.id)) += foo
    run(stmt)
  }

  override def delete(id: FooId): F[Int] = {
    val stmt = foos.filter(_.id === id).delete
    run(stmt)
  }
}

object SlickFooRepositoryInterpreter {
  class Foos(tag: Tag) extends Table[Foo](tag, "foos") {
    def id: Rep[FooId] = column[FooId]("id", O.PrimaryKey)
    def a: Rep[Int] = column[Int]("a")
    def b: Rep[Boolean] = column[Boolean]("b")

    def * : ProvenShape[Foo] = (id, a, b) <> ((Foo.apply _).tupled, Foo.unapply)
  }

  val foos = TableQuery[Foos]
}
