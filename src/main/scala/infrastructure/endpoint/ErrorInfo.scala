package org.whsv26.tapir
package infrastructure.endpoint

import sttp.model.StatusCode

case class ErrorInfo(
  statusCode: StatusCode,
  msg: String
)

object ErrorInfo {
  class EntityNotFound(entity: String, fmt: String = "id") {
    private def format(id: String) = s"$entity $id not found"
    final val format: String = format(s"{$fmt}")
    final val status: StatusCode = StatusCode.NotFound
    final def apply(id: String): ErrorInfo = ErrorInfo(status, format(id))
  }

  class EntityAlreadyExists(entity: String, fmt: String = "id") {
    private def format(id: String) = s"$entity $id already exists"
    final val format: String = format(s"{$fmt}")
    final val status: StatusCode = StatusCode.Conflict
    final def apply(id: String): ErrorInfo = ErrorInfo(status, format(id))
  }

  object Foo {
    private val entity = "Foo"
    object NotFound extends EntityNotFound(entity)
    object AlreadyExists extends EntityAlreadyExists(entity)
  }

  object User {
    private val entity = "User"
    object NotFoundByName extends EntityNotFound(entity, "name")
    object InvalidPassword {
      val format: String = "Invalid password"
      val status: StatusCode = StatusCode.BadRequest
      def apply: ErrorInfo = ErrorInfo(status, format)
    }
  }
}

