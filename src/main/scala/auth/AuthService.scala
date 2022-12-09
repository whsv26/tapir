package org.whsv26.tapir
package auth

import auth.AuthService._
import auth.User.{PasswordHash, PlainPassword}

import cats.Monad
import cats.data.{EitherT, OptionT}
import cats.effect.kernel.{Resource, Sync}

final class AuthService[F[_]: Monad](
  tokens: Tokens[F],
  users: UserRepository[F],
  hasher: Hasher[F]
) {

  def signIn(name: User.Name, pass: PlainPassword): EitherT[F, AuthError, User.Token] =
    for {
      user <- findUser(name)
      _ <- verifyPassword(pass, user.password)
      token <- EitherT.liftF(tokens.generateToken(user.id))
    } yield token

  private def findUser(
    name: User.Name
  ): EitherT[F, UserNotFound, User] =
    OptionT(users.findByName(name))
      .toRight(UserNotFound(name.value))

  private def verifyPassword(
    plain: PlainPassword,
    hashed: PasswordHash
  ): EitherT[F, InvalidPassword.type, Boolean] =
    EitherT.liftF(hasher.verifyPassword(plain, hashed))
      .ensure(InvalidPassword)(identity)
}

object AuthService {
  sealed trait AuthError extends Throwable
  case class UserNotFound(name: String) extends AuthError
  case object InvalidPassword extends AuthError

  def apply[F[_]: Sync](
    tokens: Tokens[F],
    users: UserRepository[F],
    hasher: Hasher[F]
  ): Resource[F, AuthService[F]] =
    Resource.suspend(Sync.Type.Delay) {
      new AuthService[F](tokens, users, hasher)
    }
}
