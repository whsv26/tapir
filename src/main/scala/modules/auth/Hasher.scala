package org.whsv26.tapir
package modules.auth

import modules.auth.User.{PasswordHash, PlainPassword}

import cats.effect.kernel.Sync
import distage.Id
import tsec.hashing.jca.SHA256
import tsec.passwordhashers
import tsec.passwordhashers.jca.BCrypt
import tsec.common._
import cats.syntax.functor._

trait Hasher[F[_]] {
  def hashPassword(password: PlainPassword): F[PasswordHash]
  def verifyPassword(password: PlainPassword, hash: PasswordHash): F[Boolean]
}

object Hasher {
  class BCryptImpl[F[_] : Sync](
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

  class SHA256Impl[F[_]: Sync] extends Hasher[F] {
    override def hashPassword(password: PlainPassword): F[PasswordHash] =
      SHA256
        .hash[F](password.value.utf8Bytes)
        .map(bytes => PasswordHash(bytes.toB64String))

    override def verifyPassword(password: PlainPassword, hash: PasswordHash): F[Boolean] =
      hashPassword(password).map(_ == hash)
  }
}
