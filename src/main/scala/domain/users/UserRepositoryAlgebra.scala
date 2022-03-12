package org.whsv26.tapir
package domain.users

import domain.users.Users.{UserName, UserWithPassword}

trait UserRepositoryAlgebra[F[_]] {
  def findByName(name: UserName): F[Option[UserWithPassword]]
}
