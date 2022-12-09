package org.whsv26.tapir
package foos

import foos.FooValidation.{FooAlreadyExists, FooDoesNotExist}

import cats.MonadThrow
import cats.data.EitherT
import cats.effect.kernel.{Resource, Sync}

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
      _ <- validation.exist(foo.id)
      _ <- EitherT.liftF(foos.update(foo))
    } yield foo

  def delete(id: Foo.Id): EitherT[F, FooDoesNotExist, Unit] =
    for {
      _ <- validation.exist(id)
      _ <- EitherT.liftF(foos.delete(id))
    } yield ()

  def findById(id: Foo.Id): F[Option[Foo]] =
    foos.findById(id)
}
