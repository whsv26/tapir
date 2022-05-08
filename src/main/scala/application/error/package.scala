package org.whsv26.tapir
package application

import sttp.model.StatusCode

package object error {
  case class ApiError(
    statusCode: StatusCode,
    msg: String
  )

  trait EntityApiError {
    def identity: String
    def name: String
    def action: String
    def status: StatusCode

    final val format: String =
      format(s"{$identity}")

    protected def format(identity: String) =
      s"$name $identity $action"

    final def apply(id: String): ApiError =
      ApiError(status, format(id))
  }

  class EntityNotFound(
    val name: String,
    val identity: String = "id"
  ) extends EntityApiError {
    val status: StatusCode = StatusCode.NotFound
    val action = "not found"
  }

  class EntityAlreadyExists(
    val name: String,
    val identity: String = "id"
  ) extends EntityApiError {
    val status: StatusCode = StatusCode.Conflict
    val action = "already exists"
  }
}
