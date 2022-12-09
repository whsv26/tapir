package org.whsv26.tapir
package modules.foos

import modules.foos.FooRepository.SlickImpl.foos
import modules.foos.FooValidation.FooAlreadyExists
import util.slick._

import cats.effect.Async
import cats.effect.kernel.MonadCancelThrow
import cats.implicits._
import doobie.postgres._
import doobie.util.transactor.Transactor
import eu.timepit.refined.numeric.NonNegative
import eu.timepit.refined.refineV
import slick.jdbc.JdbcBackend.DatabaseDef
import slick.lifted.ProvenShape

import java.util.UUID

trait FooRepository[F[_]] {
  def find(id: Foo.Id): F[Option[Foo]]
  def create(foo: Foo): F[Unit]
  def update(foo: Foo): F[Unit]
  def delete(id: Foo.Id): F[Unit]
}

object FooRepository {
  class SlickImpl[F[_]: Async](db: DatabaseDef) extends FooRepository[F] {
    import slick.jdbc.PostgresProfile.api._

    override def find(id: Foo.Id): F[Option[Foo]] =
      foos.filter(_.id === id.value).result.headOption.run[F](db)

    override def create(foo: Foo): F[Unit] =
      (foos += foo).run(db).void

    override def update(foo: Foo): F[Unit] =
      foos.filter(_.id === foo.id.value).update(foo).run(db).void

    override def delete(id: Foo.Id): F[Unit] =
      foos.filter(_.id === id.value).delete.run(db).void
  }

  object SlickImpl {
    import slick.jdbc.PostgresProfile.api._

    private class Foos(tag: Tag) extends Table[Foo](tag, "foos") {
      def id: Rep[UUID] = column[UUID]("id", O.PrimaryKey)
      def a: Rep[Int] = column[Int]("a")
      def b: Rep[Boolean] = column[Boolean]("b")

      def * : ProvenShape[Foo] = (id, a, b) <>[Foo](
        { case (id, a, b) => refineV[NonNegative](a).map(Foo(Foo.Id(id), _, b)).toOption.get },
        { case Foo(id, a, b) => (id.value, a.value, b).some }
      )
    }

    private val foos = TableQuery[Foos]
  }

  class DoobieImpl[F[_]: MonadCancelThrow](xa: Transactor[F]) extends FooRepository[F] {

    import doobie.implicits._

    override def find(id: Foo.Id): F[Option[Foo]] =
      sql"SELECT id, a, b FROM foos WHERE id = ${id.toString}"
        .query[Foo]
        .to[List]
        .transact(xa)
        .map(_.headOption)

    override def create(foo: Foo): F[Unit] =
      sql"INSERT INTO foos (id, a, b) values (${foo.id.value.toString}, ${foo.a.value}, ${foo.b})"
        .update
        .run
        .attemptSomeSqlState {
          case sqlstate.class23.UNIQUE_VIOLATION => FooAlreadyExists(foo.id)
        }
        .transact(xa)
        .rethrow
        .void

    override def update(foo: Foo): F[Unit] =
      sql"UPDATE foos SET a = ${foo.a.value}, b = ${foo.b} WHERE id = ${foo.id.toString}"
        .update
        .run
        .transact(xa)
        .void

    override def delete(id: Foo.Id): F[Unit] =
      sql"DELETE FROM foos WHERE id = ${id.toString}"
        .update
        .run
        .transact(xa)
        .void
  }
}
