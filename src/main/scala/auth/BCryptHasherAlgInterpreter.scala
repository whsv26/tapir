package org.whsv26.tapir
package auth

import auth.User.{PasswordHash, PlainPassword}

import cats.effect.kernel.Sync
import cats.implicits._
import distage.Id
import tsec.passwordhashers
import tsec.passwordhashers.jca.BCrypt

class BCryptHasherAlgInterpreter[F[_]: Sync]/* TODO (
  rounds: Int @Id("rounds")
)*/ extends HasherAlg[F] {

  override def hashPassword(pass: PlainPassword): F[PasswordHash] =
    BCrypt.hashpwWithRounds[F](pass.value, 12) // TODO
      .map(PasswordHash.apply)

  override def verifyPassword(lhs: PlainPassword, rhs: PasswordHash): F[Boolean] =
    BCrypt.checkpwBool[F](
      lhs.value,
      passwordhashers.PasswordHash[BCrypt](rhs.value)
    )
}
