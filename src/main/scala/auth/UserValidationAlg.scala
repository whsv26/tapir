package org.whsv26.tapir
package auth

import auth.UserValidationAlg.{UserAlreadyExists, UserDoesNotExist}

import cats.data.EitherT

trait UserValidationAlg[F[_]] {
  def doesNotExist(id: User.Id): EitherT[F, UserAlreadyExists, Unit]
  def exist(id: User.Id): EitherT[F, UserDoesNotExist, Unit]
}

object UserValidationAlg {
  sealed trait UserValidationError extends Throwable
  case class UserAlreadyExists(identity: String) extends UserValidationError
  case class UserDoesNotExist(identity: String) extends UserValidationError
}
