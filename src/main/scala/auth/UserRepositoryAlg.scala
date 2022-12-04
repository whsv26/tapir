package org.whsv26.tapir
package auth

trait UserRepositoryAlg[F[_]] {
  def findByName(name: User.Name): F[Option[User]]
}
