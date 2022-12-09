package org.whsv26.tapir

import auth.TokenAlg
import foos.FooValidation.{FooAlreadyExists, FooDoesNotExist}
import foos.create.{CreateFooEndpoint, CreateFooHandler}
import foos.delete.{DeleteFooConsumer, DeleteFooEndpoint, DeleteFooHandler, DeleteFooProducer}
import foos.read.GetFooEndpoint
import foos.update.UpdateFooEndpoint
import util.bus.{Command, Mediator}
import util.http.security.{Endpoints, ServerEndpoints}

import cats.effect.kernel.Async
import distage.{Tag, TagK}
import eu.timepit.refined.types.numeric.NonNegInt
import izumi.distage.model.definition.ModuleDef

/**
 * Transaction script for simple CRUD modules
 * Commands, Queries and Events are module public contract
 */
package object foos {
  def foosModule[F[_]: TagK] = new ModuleDef {
    make[CreateFooEndpoint[F]]
    make[CreateFooHandler[F]]
    make[DeleteFooEndpoint[F]]
    make[DeleteFooHandler[F]]
    make[DeleteFooProducer[F]]
    make[DeleteFooConsumer[F]]
    make[GetFooEndpoint[F]]
    make[UpdateFooEndpoint[F]]
    make[FooService[F]]
    make[FooRepository[F]].from[FooRepository.SlickImpl[F]]
    make[FooValidation[F]].from[FooValidation.Impl[F]]
  }

  def serverEndpoints[F[_]: Async](
    foos: FooService[F],
    tokens: TokenAlg[F],
    mediator: Mediator[F],
    deleteFooProducer: DeleteFooProducer[F]
  ): ServerEndpoints[F] =
    List(
      new CreateFooEndpoint(mediator, tokens).route,
      new GetFooEndpoint(foos, tokens).route,
      new DeleteFooEndpoint(deleteFooProducer, tokens).route,
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

  case class DeleteFooCommand(id: Foo.Id) extends Command[Either[FooDoesNotExist, Unit]]
}
