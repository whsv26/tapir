package org.whsv26.tapir
package modules.foos

import modules.foos.FooValidation.{FooAlreadyExists, FooDoesNotExist}

import cats.MonadThrow
import cats.data.EitherT

final class FooService[F[_]: MonadThrow](
  foos: FooRepository[F],
  validation: FooValidation[F],
) {

  def create(foo: Foo): EitherT[F, FooAlreadyExists, Foo] =
    for {
      _ <- validation.doesNotExist(foo.id)
      _ <- EitherT.liftF(foos.create(foo))
    } yield foo

  def update(foo: Foo): EitherT[F, FooDoesNotExist, Foo] =
    for {
      _ <- validation.exists(foo.id)
      _ <- EitherT.liftF(foos.update(foo))
    } yield foo

  def delete(id: Foo.Id): EitherT[F, FooDoesNotExist, Unit] =
    for {
      _ <- validation.exists(id)
      _ <- EitherT.liftF(foos.delete(id))
    } yield ()

  def findById(id: Foo.Id): F[Option[Foo]] =
    foos.find(id)
}
