package org.whsv26.tapir
package infrastructure.repository.inmemory

import domain.users.UserRepositoryAlgebra
import domain.users.Users.{PasswordHash, UserId, UserName, UserWithPassword}
import cats.Applicative
import cats.implicits._
import java.util.UUID

class InMemoryUserRepositoryInterpreter[F[_]: Applicative] extends UserRepositoryAlgebra[F] {

  private val id = UserId(UUID.fromString("967bbcca-9880-46b9-882c-267c04693d1c"))
  private val name = UserName("whsv26")
  private val password = PasswordHash("$2a$12$E.xFs6Acp5xZ6hvmOXm5Leg4Utb/lnOb2qmPJqn.A/4H3eFMLMLre")

  private val usersWithPassword: Map[UserId, UserWithPassword] =
    Map(id -> UserWithPassword(id, name, password))

  override def findByName(name: UserName): F[Option[UserWithPassword]] = {
    usersWithPassword
      .find { case (_, user) => user.name == name }
      .map(_._2)
      .pure[F]
  }
}