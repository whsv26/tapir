package org.whsv26.tapir
package foos

import foos.FooValidationAlg._

import cats.data.EitherT

trait FooValidationAlg[F[_]] {
  def doesNotExist(id: Foo.Id): EitherT[F, FooAlreadyExists, Unit]
  def exist(id: Foo.Id): EitherT[F, FooDoesNotExist, Unit]
}

object FooValidationAlg {
  sealed trait FooValidationError extends Throwable
  case class FooAlreadyExists(id: Foo.Id) extends FooValidationError
  case class FooDoesNotExist(id: Foo.Id) extends FooValidationError
}


