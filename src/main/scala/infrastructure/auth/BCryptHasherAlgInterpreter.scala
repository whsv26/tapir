package org.whsv26.tapir
package infrastructure.auth

import domain.auth.HasherAlg

import cats.effect.kernel.{Resource, Sync}
import cats.implicits._
import org.whsv26.tapir.domain.users.{PasswordHash, PlainPassword}
import tsec.passwordhashers
import tsec.passwordhashers.jca.BCrypt

class BCryptHasherAlgInterpreter[F[_]: Sync](
  rounds: Int
) extends HasherAlg[F] {

  override def hashPassword(pass: PlainPassword): F[PasswordHash] =
    BCrypt.hashpwWithRounds[F](pass.value, rounds)
      .map(PasswordHash.apply)

  override def verifyPassword(lhs: PlainPassword, rhs: PasswordHash): F[Boolean] =
    BCrypt.checkpwBool[F](
      lhs.value,
      passwordhashers.PasswordHash[BCrypt](rhs.value)
    )
}

object BCryptHasherAlgInterpreter {
  def apply[F[_]: Sync](
    rounds: Int
  ): Resource[F, BCryptHasherAlgInterpreter[F]] =
    Resource.suspend(Sync.Type.Delay) {
      new BCryptHasherAlgInterpreter[F](rounds)
    }
}
