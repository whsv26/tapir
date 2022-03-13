package org.whsv26.tapir
package infrastructure.endpoint

import sttp.tapir._

trait ApiEndpoint {
  type ErrorInfo = String
  val prefix: EndpointInput[Unit] = "api" / "v1"
}
