package org.whsv26.tapir
package modules.auth

import Tokens._

import cats.data.EitherT

trait Tokens[F[_]] {
  def generateToken(id: User.Id): F[User.Token]
  def verifyToken(token: User.Token): EitherT[F, UnableToVerifyToken.type, User.Id]
}

object Tokens {
  case object UnableToVerifyToken extends Throwable
}
