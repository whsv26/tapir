package org.whsv26.tapir
package domain.auth

import domain.users.{PasswordHash, PlainPassword}

trait HasherAlg[F[_]] {
  def hashPassword(pass: PlainPassword): F[PasswordHash]
  def verifyPassword(lhs: PlainPassword, rhs: PasswordHash): F[Boolean]
}
