package org.whsv26.tapir
package modules.auth

import User.PasswordHash

import cats.effect.kernel.{Resource, Sync}

import java.util.UUID

class InMemoryUserRepository[F[_]: Sync] extends UserRepository[F] {

  private val id = User.Id(UUID.fromString("967bbcca-9880-46b9-882c-267c04693d1c"))
  private val name = User.Name("whsv26")
  private val password = PasswordHash("$2a$12$E.xFs6Acp5xZ6hvmOXm5Leg4Utb/lnOb2qmPJqn.A/4H3eFMLMLre")

  private val usersWithPassword: Map[User.Id, User] =
    Map(id -> User(id, name, password))

  override def findByName(name: User.Name): F[Option[User]] =
    Sync[F].delay {
      usersWithPassword
        .find { case (_, user) => user.name == name }
        .map { case (_, user) => user }
    }
}

object InMemoryUserRepository {
  def apply[F[_]: Sync]: Resource[F, InMemoryUserRepository[F]] =
    Resource.suspend(Sync.Type.Delay) {
      new InMemoryUserRepository()
    }
}
