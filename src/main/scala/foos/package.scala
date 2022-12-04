package org.whsv26.tapir

import foos.create.CreateFooEndpoint
import foos.delete.{DeleteFooEndpoint, DeleteFooProducer}
import foos.read.GetFooEndpoint

import cats.effect.kernel.Async
import org.whsv26.tapir.auth.TokenAlg
import org.whsv26.tapir.foos.FooService
import org.whsv26.tapir.util.http.security.{Endpoints, ServerEndpoints}

package object foos {
  def serverEndpoints[F[_]: Async](
    foos: FooService[F],
    tokens: TokenAlg[F],
    producer: DeleteFooProducer[F],
  ): ServerEndpoints[F] =
    List(
      new CreateFooEndpoint(foos, tokens).route,
      new GetFooEndpoint(foos, tokens).route,
      new DeleteFooEndpoint(producer, tokens).route,
    )

  val endpoints: Endpoints = List(
    CreateFooEndpoint.route,
    GetFooEndpoint.route,
    DeleteFooEndpoint.route,
  )
}
