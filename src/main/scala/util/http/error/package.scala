package org.whsv26.tapir
package util.http

import sttp.model.StatusCode
import sttp.tapir.{EndpointOutput, statusCode, stringBody}

package object error {
  trait ApiErrorLike {
    def status: StatusCode
    def message: String

    def out: EndpointOutput[(StatusCode, String)] =
      statusCode
        .description(status, message)
        .and(stringBody)
  }

  case class ApiError(
    status: StatusCode,
    message: String
  ) extends ApiErrorLike

  trait EntityApiError extends ApiErrorLike {
    def identity: String
    def name: String
    def action: String
    def status: StatusCode

    final val message: String =
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
