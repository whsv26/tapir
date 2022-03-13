package org.whsv26.tapir
package infrastructure.endpoint

import sttp.model.StatusCode

case class ErrorInfo(statusCode: StatusCode, msg: String)

