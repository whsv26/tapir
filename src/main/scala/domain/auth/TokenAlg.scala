package org.whsv26.tapir
package domain.auth

import domain.users.UserId

import cats.data.EitherT
import TokenAlg._

trait TokenAlg[F[_]] {
  def generateToken(id: UserId): F[Token]
  def verifyToken(token: Token): EitherT[F, TokenVerificationError, UserId]
}

object TokenAlg {
  case class TokenVerificationError(cause: String) extends Throwable
}
