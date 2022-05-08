package org.whsv26.tapir
package domain.users

trait UserRepositoryAlg[F[_]] {
  def findByName(name: UserName): F[Option[UserWithPassword]]
}
