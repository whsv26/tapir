package org.whsv26.tapir
package infrastructure

import sttp.model.StatusCode

package object endpoint {
  case class ApiError(
    statusCode: StatusCode,
    msg: String
  )

  trait EntityApiError {
    def identity: String
    def name: String
    def action: String
    def status: StatusCode
    final val format: String = format(s"{$identity}")
    protected def format(identity: String) = s"$name $identity $action"
    final def apply(id: String): ApiError = ApiError(status, format(id))
  }

  class EntityNotFound(
    val name: String,
    val identity: String = "id"
  ) extends EntityApiError {
    final val status: StatusCode = StatusCode.NotFound
    override def action = "not found"
  }

  class EntityAlreadyExists(
    val name: String,
    val identity: String = "id"
  ) extends EntityApiError {
    final val status: StatusCode = StatusCode.Conflict
    override def action = "already exists"
  }
}
