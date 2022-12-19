package org.whsv26.tapir
package modules.auth

import modules.auth.User.PasswordHash

import cats.effect.kernel.Sync

import java.util.UUID

trait UserRepository[F[_]] {
  def findByName(name: User.Name): F[Option[User]]
}

object UserRepository {
  class InMemoryImpl[F[_]: Sync] extends UserRepository[F] {
    private val id = User.Id(UUID.fromString("967bbcca-9880-46b9-882c-267c04693d1c"))
    private val name = User.Name("whsv26")
    private val email = User.Email("whsv26@gmail.com")
    private val password = PasswordHash("$2a$12$E.xFs6Acp5xZ6hvmOXm5Leg4Utb/lnOb2qmPJqn.A/4H3eFMLMLre")

    private val users: Set[User] =
      Set(User(id, name, email, password))

    override def findByName(name: User.Name): F[Option[User]] =
      Sync[F].delay {
        users.find(_.name == name)
      }
  }
}
