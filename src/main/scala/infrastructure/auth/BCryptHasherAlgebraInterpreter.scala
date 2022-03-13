package org.whsv26.tapir
package infrastructure.auth

import domain.auth.HasherAlgebra
import domain.users.Users.{PasswordHash, PlainPassword}

import cats.effect.kernel.Sync
import cats.implicits._
import tsec.passwordhashers
import tsec.passwordhashers.jca.BCrypt

class BCryptHasherAlgebraInterpreter[F[_]: Sync](
  rounds: Int = 12
) extends HasherAlgebra[F] {

  override def hashPassword(pass: PlainPassword): F[PasswordHash] =
    BCrypt.hashpwWithRounds[F](pass.value, rounds)
      .map(PasswordHash.apply)

  override def verifyPassword(lhs: PlainPassword, rhs: PasswordHash): F[Boolean] =
    BCrypt.checkpwBool[F](
      lhs.value,
      passwordhashers.PasswordHash[BCrypt](rhs.value)
    )
}
