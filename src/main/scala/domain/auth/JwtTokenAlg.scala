package org.whsv26.tapir
package domain.auth

import domain.users.UserId

import cats.data.EitherT

trait JwtTokenAlg[F[_]] {
  import JwtTokenAlg._
  def generateToken(id: UserId): F[JwtToken]
  def verifyToken(token: JwtToken): EitherT[F, JwtTokenVerificationError, UserId]
}

object JwtTokenAlg {
  case class JwtTokenVerificationError(cause: String) extends Throwable
}
