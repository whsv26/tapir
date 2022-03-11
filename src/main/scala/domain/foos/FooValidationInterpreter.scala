package org.whsv26.tapir
package domain.foos

import Foo.FooId
import FooValidationAlgebra._
import cats.Functor
import cats.data.EitherT
import cats.implicits._
import FooValidationAlgebra.FooDoesNotExist

class FooValidationInterpreter[F[_]: Functor](
  foos: FooRepositoryAlgebra[F]
) extends FooValidationAlgebra[F] {

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




