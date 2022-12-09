package org.whsv26.tapir
package auth

import auth.Tokens._

import cats.data.EitherT

trait Tokens[F[_]] {
  def generateToken(id: User.Id): F[User.Token]
  def verifyToken(token: User.Token): EitherT[F, TokenVerificationError, User.Id]
}

object Tokens {
  case class TokenVerificationError(cause: String) extends Throwable
}
