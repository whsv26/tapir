package org.whsv26.tapir
package domain.users

import domain.users.UserValidationAlgebra.{UserAlreadyExists, UserDoesNotExist}
import domain.users.Users._
import cats.data.EitherT

trait UserValidationAlgebra[F[_]] {
  def doesNotExist(id: UserId): EitherT[F, UserAlreadyExists, Unit]
  def exist(id: UserId): EitherT[F, UserDoesNotExist, Unit]
}

object UserValidationAlgebra {
  sealed trait UserValidationError extends Throwable
  case class UserAlreadyExists(identity: String) extends UserValidationError
  case class UserDoesNotExist(identity: String) extends UserValidationError
}
