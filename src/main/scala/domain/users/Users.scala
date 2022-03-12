package org.whsv26.tapir
package domain.users

import java.util.UUID

object Users {
  case class User(
    id: UserId,
    name: UserName
  )

  case class UserWithPassword(
    id: UserId,
    name: UserName,
    password: PasswordHash
  )

  sealed trait UserIdentity
  case class UserId(value: UUID) extends UserIdentity
  case class UserName(value: String) extends UserIdentity

  case class PlainPassword(value: String)
  case class PasswordHash(value: String)

}
