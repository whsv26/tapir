package org.whsv26.tapir
package auth

import auth.User.{PasswordHash, PlainPassword}

trait Hasher[F[_]] {
  def hashPassword(pass: PlainPassword): F[PasswordHash]
  def verifyPassword(lhs: PlainPassword, rhs: PasswordHash): F[Boolean]
}
