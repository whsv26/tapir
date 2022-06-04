package org.whsv26.tapir
package domain.foos

import org.whsv26.tapir.application.endpoint.foos.CreateFooEndpoint.CreateFoo
import domain.foos.FooValidationAlg.{FooAlreadyExists, FooDoesNotExist}

import cats.MonadThrow
import cats.data.EitherT
import cats.effect.kernel.{Resource, Sync}

final class FooService[F[_]: MonadThrow](
  foos: FooRepositoryAlg[F],
  validation: FooValidationAlg[F],
) {

  def create(id: FooId, createFoo: CreateFoo): EitherT[F, FooAlreadyExists, FooId] =
    for {
      _ <- validation.doesNotExist(id)
      foo <- EitherT.liftF(foos.create(id, createFoo))
    } yield foo.id

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
