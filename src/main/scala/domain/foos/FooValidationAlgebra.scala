package org.whsv26.tapir
package domain.foos

import Foo.FooId
import FooValidationAlgebra._
import cats.data.EitherT

trait FooValidationAlgebra[F[_]] {
  def doesNotExist(id: FooId): EitherT[F, FooAlreadyExists, Unit]
  def exist(id: FooId): EitherT[F, FooDoesNotExist, Unit]
}

object FooValidationAlgebra {
  sealed trait FooValidationError extends Throwable
  case class FooAlreadyExists(fooId: FooId) extends FooValidationError
  case class FooDoesNotExist(fooId: FooId) extends FooValidationError
}


