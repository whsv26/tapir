package org.whsv26.tapir
package infrastructure.auth

import domain.auth.HasherAlg
import domain.users.{PasswordHash, PlainPassword}

import cats.effect.kernel.Sync
import cats.implicits._
import tsec.common._
import tsec.hashing.jca.SHA256

class SHA256HasherAlgInterpreter[F[_]: Sync] extends HasherAlg[F] {
  override def hashPassword(pass: PlainPassword): F[PasswordHash] =
    SHA256.hash[F](pass.value.utf8Bytes)
      .map(bytes => PasswordHash(bytes.toB64String))

  override def verifyPassword(lhs: PlainPassword, rhs: PasswordHash): F[Boolean] =
    hashPassword(lhs)
      .map(_ == rhs)

}
