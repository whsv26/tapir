package org.whsv26.tapir
package domain.auth

import domain.auth.AuthService._
import domain.users._

import cats.Monad
import cats.data.{EitherT, OptionT}

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
  ): EitherT[F, UserNotFoundByName, UserWithPassword] =
    OptionT(users.findByName(name))
      .toRight(UserNotFoundByName(name.value))

  private def verifyPassword(
    plain: PlainPassword,
    hashed: PasswordHash
  ): EitherT[F, InvalidUserPassword.type, Boolean] =
    EitherT.liftF(hasher.verifyPassword(plain, hashed))
      .ensure(InvalidUserPassword)(identity)
}

object AuthService {
  sealed trait AuthError extends Throwable
  case class UserNotFoundByName(name: String) extends AuthError
  case object InvalidUserPassword extends AuthError
}
