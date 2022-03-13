package org.whsv26.tapir
package domain.auth

import domain.auth.JwtTokenAlgebra.JwtTokenVerificationError
import domain.users.Users.UserId
import cats.data.EitherT

trait JwtTokenAlgebra[F[_]] {
  def generateToken(id: UserId): F[JwtToken]
  def verifyToken(token: JwtToken): EitherT[F, JwtTokenVerificationError, UserId]
}

object JwtTokenAlgebra {
  case class JwtTokenVerificationError(cause: String) extends Throwable
}
