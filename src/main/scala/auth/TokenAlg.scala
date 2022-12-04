package org.whsv26.tapir
package auth

import auth.TokenAlg._

import cats.data.EitherT

trait TokenAlg[F[_]] {
  def generateToken(id: User.Id): F[User.Token]
  def verifyToken(token: User.Token): EitherT[F, TokenVerificationError, User.Id]
}

object TokenAlg {
  case class TokenVerificationError(cause: String) extends Throwable
}
