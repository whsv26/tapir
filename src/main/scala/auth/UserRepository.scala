package org.whsv26.tapir
package auth

trait UserRepository[F[_]] {
  def findByName(name: User.Name): F[Option[User]]
}
