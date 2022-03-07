package org.whsv26.tapir

import Foo.FooId
import FooService.FooAlreadyExists
import cats.data.EitherT
import cats.effect.kernel.Sync
import cats.implicits._

class FooService[F[_]: Sync](foos: FooRepositoryAlgebra[F]) {
  def create(foo: Foo): EitherT[F, FooAlreadyExists, FooId] = {
    val fb = for {
      _ <- foos
        .findById(foo.id)
        .ensure(FooAlreadyExists(foo))(_.isEmpty)
      id <- foos.create(foo)
    } yield id

    EitherT.liftF(fb)
  }

  def delete(id: FooId): F[Int] = foos.delete(id)

  def findById(id: FooId): F[Option[Foo]] = foos.findById(id)
}

object FooService {
  sealed trait FooError extends Throwable
  case class FooAlreadyExists(foo: Foo) extends FooError
}
