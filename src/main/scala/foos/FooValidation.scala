package org.whsv26.tapir
package foos

import foos.FooValidation._

import cats.Functor
import cats.data.EitherT
import cats.syntax.functor._

trait FooValidation[F[_]] {
  def doesNotExist(id: Foo.Id): EitherT[F, FooAlreadyExists, Unit]
  def exist(id: Foo.Id): EitherT[F, FooDoesNotExist, Unit]
}

object FooValidation {
  sealed trait FooValidationError extends Throwable
  case class FooAlreadyExists(id: Foo.Id) extends FooValidationError
  case class FooDoesNotExist(id: Foo.Id) extends FooValidationError

  class Impl[F[_]: Functor](foos: FooRepository[F]) extends FooValidation[F] {

    override def doesNotExist(id: Foo.Id): EitherT[F, FooAlreadyExists, Unit] =
      EitherT {
        foos.find(id).map {
          case Some(_) => Left(FooAlreadyExists(id))
          case None => Right(())
        }
      }

    override def exist(id: Foo.Id): EitherT[F, FooDoesNotExist, Unit] =
      EitherT.fromOptionF(
        foos.find(id),
        FooDoesNotExist(id)
      ).void
  }
}
