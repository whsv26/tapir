package org.whsv26.tapir
package domain.foos

import FooValidationAlg._
import cats.Functor
import cats.data.EitherT
import cats.implicits._
import FooValidationAlg.FooDoesNotExist
import cats.effect.kernel.{Resource, Sync}

class FooValidationInterpreter[F[_]: Functor](
  foos: FooRepositoryAlg[F]
) extends FooValidationAlg[F] {

  override def doesNotExist(id: FooId): EitherT[F, FooAlreadyExists, Unit] =
    EitherT {
      foos.findById(id).map {
        case Some(_) => Left(FooAlreadyExists(id))
        case None => Right(())
      }
    }

  override def exist(id: FooId): EitherT[F, FooDoesNotExist, Unit] =
    EitherT.fromOptionF(
      foos.findById(id),
      FooDoesNotExist(id)
    ).void
}

object FooValidationInterpreter {
  def apply[F[_]: Sync](
    foos: FooRepositoryAlg[F]
  ): Resource[F, FooValidationInterpreter[F]] =
    Resource.suspend(Sync.Type.Delay) {
      new FooValidationInterpreter(foos)
    }
}




