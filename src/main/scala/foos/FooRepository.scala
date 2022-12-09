package org.whsv26.tapir
package foos

import foos.FooRepository.SlickImpl.foos
import util.slick._

import cats.effect.Async
import cats.implicits._
import eu.timepit.refined.numeric.NonNegative
import eu.timepit.refined.refineV
import slick.jdbc.JdbcBackend.DatabaseDef
import slick.jdbc.PostgresProfile.api._
import slick.lifted.ProvenShape

import java.util.UUID

trait FooRepository[F[_]] {
  def findById(id: Foo.Id): F[Option[Foo]]
  def create(foo: Foo): F[Int]
  def update(foo: Foo): F[Int]
  def delete(id: Foo.Id): F[Unit]
}

object FooRepository {
  class SlickImpl[F[_]: Async](db: DatabaseDef) extends FooRepository[F] {

    override def findById(id: Foo.Id): F[Option[Foo]] =
      foos.filter(_.id === id.value).result.headOption.run[F](db)

    override def create(foo: Foo): F[Int] =
      (foos += foo).run(db)

    override def update(foo: Foo): F[Int] =
      foos.filter(_.id === foo.id.value).update(foo).run(db)

    override def delete(id: Foo.Id): F[Unit] =
      foos.filter(_.id === id.value).delete.run(db).void
  }

  object SlickImpl {
    private class Foos(tag: Tag) extends Table[Foo](tag, "foos") {
      def id: Rep[UUID] = column[UUID]("id", O.PrimaryKey)
      def a: Rep[Int] = column[Int]("a")
      def b: Rep[Boolean] = column[Boolean]("b")

      def * : ProvenShape[Foo] = (id, a, b) <>[Foo]({
        case (id, a, b) =>
          Foo(
            Foo.Id(id),
            refineV[NonNegative](a).toOption.get, // TODO
            b
          )
      }, {
        case Foo(id, a, b) => (id.value, a.value, b).some
      })
    }

    private val foos = TableQuery[Foos]
  }
}
