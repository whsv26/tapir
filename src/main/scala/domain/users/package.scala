package org.whsv26.tapir
package domain

import java.util.UUID

package object users {
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
