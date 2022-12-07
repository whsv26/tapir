package org.whsv26.tapir
package auth

import cats.effect.kernel.{Resource, Sync}
import cats.implicits._
import User.{PasswordHash, PlainPassword}
import com.softwaremill.tagging.@@
import org.whsv26.tapir.auth.BCryptHasherAlgInterpreter.RoundsTag
import tsec.passwordhashers
import tsec.passwordhashers.jca.BCrypt

class BCryptHasherAlgInterpreter[F[_]: Sync](
  rounds: Int @@ RoundsTag
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
    rounds: Int @@ RoundsTag
  ): Resource[F, BCryptHasherAlgInterpreter[F]] =
    Resource.suspend(Sync.Type.Delay) {
      new BCryptHasherAlgInterpreter[F](rounds)
    }

  trait RoundsTag
}
