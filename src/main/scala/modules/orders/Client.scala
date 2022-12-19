package org.whsv26.tapir
package modules.orders

import modules.orders.Client.{Name, Phone}

import eu.timepit.refined.api.Refined
import eu.timepit.refined.string.MatchesRegex
import eu.timepit.refined.types.string.NonEmptyString
import io.estatico.newtype.macros.newtype

import java.util.UUID

case class Client(
  phone: Phone,
  name: Option[Name]
)

object Client {
  type PhoneRegex = """/\(?([0-9]{3})\)?([ .-]?)([0-9]{3})\2([0-9]{4})/"""

  @newtype case class Id(value: UUID)

  @newtype case class Phone(value: String Refined MatchesRegex[PhoneRegex])

  @newtype case class Name(value: NonEmptyString)

}
