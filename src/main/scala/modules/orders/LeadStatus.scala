package org.whsv26.tapir
package modules.orders

import enumeratum.{CirceEnum, Enum, EnumEntry}
import org.whsv26.tapir.modules.orders.LeadStatus.Rejected.Reason

sealed trait LeadStatus

object LeadStatus {
  case object New extends LeadStatus

  case object RecallLater extends LeadStatus

  case class NoAnswer(attempts: Int) extends LeadStatus

  case class Rejected(reason: Reason) extends LeadStatus

  object Rejected {
    sealed trait Reason
    case object Expensive extends Reason
    case object Spam extends Reason
    case class Custom(text: String) extends Reason
  }

  case object Approved extends LeadStatus
}
