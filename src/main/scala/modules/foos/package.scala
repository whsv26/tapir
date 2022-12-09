package org.whsv26.tapir
package modules

import foos.FooValidation.{FooAlreadyExists, FooDoesNotExist}
import foos.create.{CreateFooEndpoint, CreateFooHandler}
import foos.delete.{DeleteFooConsumer, DeleteFooEndpoint, DeleteFooHandler, DeleteFooProducer}
import foos.read.GetFooEndpoint
import foos.update.UpdateFooEndpoint
import util.bus.{Command, NotificationHandlerBase, RequestHandlerBase}
import util.http.security.Endpoints

import distage.TagK
import eu.timepit.refined.types.numeric.NonNegInt
import izumi.distage.model.definition.ModuleDef
import sttp.tapir.server.ServerEndpoint

/**
 * Transaction script for simple CRUD modules
 * Commands, Queries and Events are module public contract
 */
package object foos {
  def module[F[_]: TagK] = new ModuleDef {
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

    many[ServerEndpoint[Any, F]]
      .add((e: CreateFooEndpoint[F]) => e.route)
      .add((e: DeleteFooEndpoint[F]) => e.route)
      .add((e: UpdateFooEndpoint[F]) => e.route)
      .add((e: GetFooEndpoint[F]) => e.route)

    many[RequestHandlerBase[F]]
      .add((h: CreateFooHandler[F]) => h)
      .add((h: DeleteFooHandler[F]) => h)

    many[NotificationHandlerBase[F]]
  }

  val endpoints: Endpoints = List(
    CreateFooEndpoint.route,
    GetFooEndpoint.route,
    UpdateFooEndpoint.route,
    DeleteFooEndpoint.route,
  )

  case class CreateFooCommand(
    id: Foo.Id,
    a: NonNegInt,
    b: Boolean
  ) extends Command[Either[FooAlreadyExists, Foo]]

  case class DeleteFooCommand(id: Foo.Id) extends Command[Either[FooDoesNotExist, Unit]]
}
