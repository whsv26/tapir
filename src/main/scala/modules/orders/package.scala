package org.whsv26.tapir
package modules

import util.bus.Command

package object orders {
  case class CreateLead(
    client: Client,
    items: List[LeadItem],
  ) extends Command[Unit]

}
