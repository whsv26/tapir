package org.whsv26.tapir
package infrastructure

import sttp.model.StatusCode

package object endpoint {
  case class ApiError(
    statusCode: StatusCode,
    msg: String
  )

  class EntityNotFound(entity: String, fmt: String = "id") {
    private def format(id: String) = s"$entity $id not found"
    final val format: String = format(s"{$fmt}")
    final val status: StatusCode = StatusCode.NotFound
    final def apply(id: String): ApiError = ApiError(status, format(id))
  }

  class EntityAlreadyExists(entity: String, fmt: String = "id") {
    private def format(id: String) = s"$entity $id already exists"
    final val format: String = format(s"{$fmt}")
    final val status: StatusCode = StatusCode.Conflict
    final def apply(id: String): ApiError = ApiError(status, format(id))
  }
}
