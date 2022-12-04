package org.whsv26.tapir
package foos

import foos.FooValidationAlg._

import cats.Functor
import cats.data.EitherT
import cats.effect.kernel.{Resource, Sync}
import cats.implicits._

class FooValidationAlgInterpreter[F[_]: Functor](
  foos: FooRepositoryAlg[F]
) extends FooValidationAlg[F] {

  override def doesNotExist(id: Foo.Id): EitherT[F, FooAlreadyExists, Unit] =
    EitherT {
      foos.findById(id).map {
        case Some(_) => Left(FooAlreadyExists(id))
        case None => Right(())
      }
    }

  override def exist(id: Foo.Id): EitherT[F, FooDoesNotExist, Unit] =
    EitherT.fromOptionF(
      foos.findById(id),
      FooDoesNotExist(id)
    ).void
}

object FooValidationAlgInterpreter {
  def apply[F[_]: Sync](
    foos: FooRepositoryAlg[F]
  ): Resource[F, FooValidationAlgInterpreter[F]] =
    Resource.suspend(Sync.Type.Delay) {
      new FooValidationAlgInterpreter(foos)
    }
}




