package org.whsv26.tapir
package domain.foos

import domain.foos.Foo.FooId
import domain.foos.FooValidationAlgebra.{FooAlreadyExists, FooDoesNotExist}
import infrastructure.endpoint.foos.CreateFooEndpoint.CreateFoo

import cats.MonadThrow
import cats.data.EitherT

class FooService[F[_]: MonadThrow](
  foos: FooRepositoryAlgebra[F],
  validation: FooValidationAlgebra[F],
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
