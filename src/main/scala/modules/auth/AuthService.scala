package org.whsv26.tapir
package modules.auth

import modules.auth.AuthService._
import modules.auth.User.{PasswordHash, PlainPassword}

import cats.Monad
import cats.data.{EitherT, OptionT}
import cats.syntax.functor._

final class AuthService[F[_]: Monad](
  tokens: Tokens[F],
  users: UserRepository[F],
  hasher: Hasher[F]
) {

  def signIn(name: User.Name, pass: PlainPassword): EitherT[F, AuthError, User.Token] =
    for {
      user <- OptionT(users.findByName(name)).toRight(UserNotFound(name.value))
      _ <- verifyPassword(pass, user.password)
      token <- EitherT.liftF(tokens.generateToken(user.id))
    } yield token

  private def verifyPassword(
    plain: PlainPassword,
    hashed: PasswordHash
  ): EitherT[F, InvalidPassword.type, Unit] =
    EitherT
      .liftF(hasher.verifyPassword(plain, hashed))
      .ensure(InvalidPassword)(identity)
      .void
}

object AuthService {
  sealed trait AuthError extends Throwable
  case class UserNotFound(name: String) extends AuthError
  case object InvalidPassword extends AuthError
}
