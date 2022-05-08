package org.whsv26.tapir
package domain

import io.estatico.newtype.macros.newtype

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

  @newtype case class UserId(value: UUID)
  @newtype case class UserName(value: String)
  @newtype case class PlainPassword(value: String)
  @newtype case class PasswordHash(value: String)
}
