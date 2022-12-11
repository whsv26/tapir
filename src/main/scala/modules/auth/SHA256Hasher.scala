package org.whsv26.tapir
package modules.auth

import cats.effect.kernel.Sync
import cats.implicits._
import User.{PasswordHash, PlainPassword}
import tsec.common._
import tsec.hashing.jca.SHA256

class SHA256Hasher[F[_]: Sync] extends Hasher[F] {
  override def hashPassword(password: PlainPassword): F[PasswordHash] =
    SHA256.hash[F](password.value.utf8Bytes)
      .map(bytes => PasswordHash(bytes.toB64String))

  override def verifyPassword(password: PlainPassword, hash: PasswordHash): F[Boolean] =
    hashPassword(password)
      .map(_ == hash)

}
