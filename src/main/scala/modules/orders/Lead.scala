package org.whsv26.tapir
package modules.orders

import enumeratum.{CirceEnum, Enum, EnumEntry}
import io.circe.{Decoder, Encoder}
import io.estatico.newtype.macros.newtype
import squants.market.Money

import java.util.UUID

/**
 * Clicking on an internet ad for your product
 * and filling in a form
 */
case class Lead(
  id: Lead.Id,
  status: LeadStatus,
  client: Client,
  items: List[LeadItem],
  total: Money
)

object Lead {
  @newtype case class Id(value: UUID)

  object Id {
    def next: Id = Id(UUID.randomUUID())

    implicit val decoder: Decoder[Id] = deriving
    implicit val encoder: Encoder[Id] = deriving
  }
}
