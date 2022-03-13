package org.whsv26.tapir
package domain.auth

import domain.users.Users.{PasswordHash, PlainPassword}

import cats.effect.kernel.Sync
import tsec.passwordhashers.jca.BCrypt
import cats.implicits._
import tsec.passwordhashers

trait PasswordHasherAlgebra[F[_]] {
  def hashPassword(pass: PlainPassword): F[PasswordHash]
  def verifyPassword(lhs: PlainPassword, rhs: PasswordHash): F[Boolean]
}

object PasswordHasherAlgebra {
  private val DefaultRounds = 12

  def apply[F[_]: Sync](rounds: Int = DefaultRounds): PasswordHasherAlgebra[F] = new PasswordHasherAlgebra[F] {
    override def hashPassword(pass: PlainPassword): F[PasswordHash] =
      BCrypt.hashpwWithRounds[F](pass.value, rounds)
        .map(PasswordHash.apply)

    override def verifyPassword(lhs: PlainPassword, rhs: PasswordHash): F[Boolean] =
      BCrypt.checkpwBool[F](
        lhs.value,
        passwordhashers.PasswordHash[BCrypt](rhs.value)
      )
  }
}
