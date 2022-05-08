package org.whsv26.tapir
package domain.users

import domain.users.UserValidationAlg.{UserAlreadyExists, UserDoesNotExist}
import cats.data.EitherT

trait UserValidationAlg[F[_]] {
  def doesNotExist(id: UserId): EitherT[F, UserAlreadyExists, Unit]
  def exist(id: UserId): EitherT[F, UserDoesNotExist, Unit]
}

object UserValidationAlg {
  sealed trait UserValidationError extends Throwable
  case class UserAlreadyExists(identity: String) extends UserValidationError
  case class UserDoesNotExist(identity: String) extends UserValidationError
}
