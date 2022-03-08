package org.whsv26.tapir

import Foo.FooId
import FooValidationAlgebra.{FooAlreadyExists, FooDoesNotExist}
import cats.MonadThrow
import cats.data.EitherT

class FooService[F[_]: MonadThrow](
  foos: FooRepositoryAlgebra[F],
  validation: FooValidationAlgebra[F],
) {

  def create(foo: Foo): EitherT[F, FooAlreadyExists, FooId] =
    for {
      _ <- validation.doesNotExist(foo.id)
      id <- EitherT.liftF(foos.create(foo))
    } yield id

  def delete(id: FooId): EitherT[F, FooDoesNotExist, Int] =
    for {
      _ <- validation.exist(id)
      id <- EitherT.liftF(foos.delete(id))
    } yield id


  def findById(id: FooId): F[Option[Foo]] =
    foos.findById(id)
}
