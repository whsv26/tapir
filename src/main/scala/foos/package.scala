package org.whsv26.tapir

import auth.TokenAlg
import foos.create.CreateFooEndpoint
import foos.delete.{DeleteFooEndpoint, DeleteFooProducer}
import foos.read.GetFooEndpoint
import util.bus.{Command, Mediator}
import util.http.security.{Endpoints, ServerEndpoints}

import cats.effect.kernel.Async
import eu.timepit.refined.types.numeric.NonNegInt
import org.whsv26.tapir.foos.FooValidation.FooAlreadyExists

/**
 * Transaction script for simple CRUD modules
 * Commands, Queries and Events are module public contract
 */
package object foos {
  def serverEndpoints[F[_]: Async](
    foos: FooService[F],
    tokens: TokenAlg[F],
    producer: DeleteFooProducer[F],
    mediator: Mediator[F]
  ): ServerEndpoints[F] =
    List(
      new CreateFooEndpoint(mediator, tokens).route,
      new GetFooEndpoint(foos, tokens).route,
      new DeleteFooEndpoint(producer, tokens).route,
    )

  val endpoints: Endpoints = List(
    CreateFooEndpoint.route,
    GetFooEndpoint.route,
    DeleteFooEndpoint.route,
  )

  case class CreateFooCommand(
    id: Foo.Id,
    a: NonNegInt,
    b: Boolean
  ) extends Command[Either[FooAlreadyExists, Foo]]

  case class DeleteFooCommand(id: Foo.Id) extends Command[Unit]
}
