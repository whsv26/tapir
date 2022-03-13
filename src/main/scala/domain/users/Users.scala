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

  case class UserId(value: UUID)
  case class UserName(value: String)
  case class PlainPassword(value: String)
  case class PasswordHash(value: String)

}
