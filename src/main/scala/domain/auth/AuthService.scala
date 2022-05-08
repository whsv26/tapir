package org.whsv26.tapir
package domain.auth

import domain.auth.AuthService._
import domain.users.{PlainPassword, UserName, UserRepositoryAlg}

import cats.Monad
import cats.data.EitherT

class AuthService[F[+_]: Monad](
  tokens: JwtTokenAlg[F],
  users: UserRepositoryAlg[F],
  hasher: HasherAlg[F]
) {

  def signIn(name: UserName, pass: PlainPassword): EitherT[F, AuthError, JwtToken] =
    for {
      // find user
      user <- EitherT.fromOptionF(
        users.findByName(name),
        UserNotFound(name.value)
      )

      // verify password
      _ <- EitherT.liftF(hasher.verifyPassword(pass, user.password))
        .ensure(InvalidPassword)(identity)

      // generate jwt token
      token <- EitherT.liftF(tokens.generateToken(user.id))

    } yield token
}

object AuthService {
  sealed trait AuthError extends Throwable
  case class UserNotFound(name: String) extends AuthError
  case object InvalidPassword extends AuthError
}
