package org.whsv26.tapir
package domain.auth

import domain.users.Users.{PasswordHash, PlainPassword}

import cats.Id

trait PasswordHasherAlgebra[F[_]] {
  def hashPassword(pass: PlainPassword): F[PasswordHash]
  def verifyPassword(lhs: PlainPassword, rhs: PasswordHash): F[Boolean]
}

object PasswordHasherAlgebra {
  def apply(): PasswordHasherAlgebra[Id] = new PasswordHasherAlgebra[Id] {
    override def hashPassword(pass: PlainPassword): PasswordHash = ???

    override def verifyPassword(lhs: PlainPassword, rhs: PasswordHash): Id[Boolean] = ???
  }
}
