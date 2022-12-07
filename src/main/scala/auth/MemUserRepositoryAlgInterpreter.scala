package org.whsv26.tapir
package auth

import auth.User.PasswordHash

import cats.effect.kernel.{Resource, Sync}

import java.util.UUID

class MemUserRepositoryAlgInterpreter[F[_]: Sync] extends UserRepositoryAlg[F] {

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

object MemUserRepositoryAlgInterpreter {
  def apply[F[_]: Sync]: Resource[F, MemUserRepositoryAlgInterpreter[F]] =
    Resource.suspend(Sync.Type.Delay) {
      new MemUserRepositoryAlgInterpreter()
    }
}