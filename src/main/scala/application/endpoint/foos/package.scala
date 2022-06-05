package org.whsv26.tapir
package application.endpoint

import application.security.{Endpoints, ServerEndpoints}
import domain.auth.TokenAlg
import domain.foos.FooService
import infrastructure.messaging.kafka.DeleteFooProducer

import cats.effect.kernel.Async

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
