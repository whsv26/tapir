package org.whsv26.tapir
package infrastructure.endpoint

import sttp.tapir._

trait ApiEndpoint {
  protected val prefix: EndpointInput[Unit] = "api" / "v1"
}
