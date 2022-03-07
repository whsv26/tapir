package org.whsv26.tapir

import Foo.FooId
import FooService.FooAlreadyExists
import cats.MonadThrow
import cats.data.EitherT
import cats.implicits._

class FooService[F[_]: MonadThrow](foos: FooRepositoryAlgebra[F]) {
  def create(foo: Foo): EitherT[F, FooAlreadyExists, FooId] = {
    val fb = for {
      _ <- foos
        .findById(foo.id)
        .ensure(FooAlreadyExists(foo.id))(_.isEmpty)
      id <- foos.create(foo)
    } yield id

    EitherT(fb.attemptNarrow[FooAlreadyExists])
  }

  def delete(id: FooId): F[Int] = foos.delete(id)

  def findById(id: FooId): F[Option[Foo]] = foos.findById(id)
}

object FooService {
  sealed trait FooError extends Throwable
  case class FooAlreadyExists(fooId: FooId) extends FooError
}
