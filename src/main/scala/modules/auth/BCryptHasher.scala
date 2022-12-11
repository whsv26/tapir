package org.whsv26.tapir
package modules.auth

import User.{PasswordHash, PlainPassword}
import cats.effect.kernel.Sync
import cats.implicits._
import distage.Id
import tsec.passwordhashers
import tsec.passwordhashers.jca.BCrypt

class BCryptHasher[F[_]: Sync](
  rounds: Int @Id("rounds")
) extends Hasher[F] {

  override def hashPassword(password: PlainPassword): F[PasswordHash] =
    BCrypt.hashpwWithRounds[F](password.value, rounds)
      .map(PasswordHash.apply)

  override def verifyPassword(password: PlainPassword, hash: PasswordHash): F[Boolean] =
    BCrypt.checkpwBool[F](
      password.value,
      passwordhashers.PasswordHash[BCrypt](hash.value)
    )
}
