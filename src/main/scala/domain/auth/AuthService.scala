package org.whsv26.tapir
package domain.auth

import domain.auth.AuthService._
import domain.users._

import cats.Monad
import cats.data.{EitherT, OptionT}
import cats.effect.kernel.{Resource, Sync}

final class AuthService[F[_]: Monad](
  tokens: TokenAlg[F],
  users: UserRepositoryAlg[F],
  hasher: HasherAlg[F]
) {

  def signIn(name: UserName, pass: PlainPassword): EitherT[F, AuthError, Token] =
    for {
      user <- findUser(name)
      _ <- verifyPassword(pass, user.password)
      token <- EitherT.liftF(tokens.generateToken(user.id))
    } yield token

  private def findUser(
    name: UserName
  ): EitherT[F, UserNotFound, UserWithPassword] =
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
    tokens: TokenAlg[F],
    users: UserRepositoryAlg[F],
    hasher: HasherAlg[F]
  ): Resource[F, AuthService[F]] =
    Resource.suspend(Sync.Type.Delay) {
      new AuthService[F](tokens, users, hasher)
    }
}
