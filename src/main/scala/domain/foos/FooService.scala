package org.whsv26.tapir
package domain.foos

import application.endpoint.foos.CreateFooEndpoint.CreateFoo
import application.endpoint.foos.UpdateFooEndpoint.UpdateFoo
import domain.foos.FooValidationAlg.{FooAlreadyExists, FooDoesNotExist}

import cats.MonadThrow
import cats.data.EitherT
import cats.effect.kernel.{Resource, Sync}

final class FooService[F[_]: MonadThrow](
  foos: FooRepositoryAlg[F],
  validation: FooValidationAlg[F],
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

  def delete(id: FooId): EitherT[F, FooDoesNotExist, Unit] =
    for {
      _ <- validation.exist(id)
      _ <- EitherT.liftF(foos.delete(id))
    } yield ()

  def findById(id: FooId): F[Option[Foo]] =
    foos.findById(id)
}

object FooService {
  def apply[F[_]: Sync](
    foos: FooRepositoryAlg[F],
    validation: FooValidationAlg[F],
  ): Resource[F, FooService[F]] =
    Resource.suspend(Sync.Type.Delay) {
      new FooService[F](foos, validation)
    }
}
