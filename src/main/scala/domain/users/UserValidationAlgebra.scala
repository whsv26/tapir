package org.whsv26.tapir
package domain.users

import domain.users.UserValidationAlgebra.{UserAlreadyExists, UserDoesNotExist}
import domain.users.Users.{UserId, UserIdentity}
import cats.data.EitherT

trait UserValidationAlgebra[F[_]] {
  def doesNotExist(id: UserId): EitherT[F, UserAlreadyExists, Unit]
  def exist(id: UserId): EitherT[F, UserDoesNotExist, Unit]
}

object UserValidationAlgebra {
  sealed trait UserValidationError extends Throwable
  case class UserAlreadyExists(id: UserIdentity) extends UserValidationError
  case class UserDoesNotExist(id: UserIdentity) extends UserValidationError
}
