package org.whsv26.tapir
package infrastructure.endpoint

import sttp.tapir._

trait ApiEndpoint {
  val prefix: EndpointInput[Unit] = "api" / "v1"
}
