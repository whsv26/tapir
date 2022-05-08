package org.whsv26.tapir
package domain.foos

import Foo.FooId
import FooValidationAlg._
import cats.data.EitherT

trait FooValidationAlg[F[_]] {
  def doesNotExist(id: FooId): EitherT[F, FooAlreadyExists, Unit]
  def exist(id: FooId): EitherT[F, FooDoesNotExist, Unit]
}

object FooValidationAlg {
  sealed trait FooValidationError extends Throwable
  case class FooAlreadyExists(fooId: FooId) extends FooValidationError
  case class FooDoesNotExist(fooId: FooId) extends FooValidationError
}


