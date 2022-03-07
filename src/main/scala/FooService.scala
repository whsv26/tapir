package org.whsv26.tapir

import FooService.FooAlreadyExists
import cats.MonadThrow
import cats.effect.kernel.Sync
import cats.implicits._
import java.util.UUID

class FooService[F[_]: Sync](foos: FooRepositoryAlgebra[F]) {
  def create(foo: Foo): F[UUID] = for {
    isPresent <- foos.findById(foo.id).map(_.isDefined)
    _ <- MonadThrow[F].raiseWhen(isPresent)(FooAlreadyExists(foo))
    id <- foos.create(foo)
  } yield id

  def delete(id: UUID): F[Int] = foos.delete(id)

  def findById(id: UUID): F[Option[Foo]] = foos.findById(id)
}

object FooService {
  sealed trait FooError extends Throwable
  case class FooAlreadyExists(foo: Foo) extends FooError
}
