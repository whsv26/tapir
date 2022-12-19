package org.whsv26.tapir
package modules.orders

import eu.timepit.refined.types.numeric.NonNegInt
import io.estatico.newtype.macros.newtype
import squants.market.{Money, USD}

import java.util.UUID

case class LeadItem(
  id: LeadItem.Id,
  quantity: LeadItem.Quantity,
  price: Money
) {
  def subTotal: Money =
    USD(price.amount * quantity.value.value)
}

object LeadItem {
  @newtype case class Id(value: UUID)

  @newtype case class Quantity(value: NonNegInt)
}