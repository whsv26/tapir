package org.whsv26.tapir
package modules.auth

import modules.auth.User.{PasswordHash, PlainPassword}

trait Hasher[F[_]] {
  def hashPassword(password: PlainPassword): F[PasswordHash]
  def verifyPassword(password: PlainPassword, hash: PasswordHash): F[Boolean]
}
