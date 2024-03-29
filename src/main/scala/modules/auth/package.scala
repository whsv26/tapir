package org.whsv26.tapir
package modules

import util.bus.{NotificationHandlerBase, RequestHandlerBase}
import util.http.security.Endpoints

import distage.TagK
import izumi.distage.model.definition.ModuleDef
import sttp.tapir.server.ServerEndpoint

package object auth {
  def module[F[_]: TagK] = new ModuleDef {
    make[Int].named("rounds").from(12)
    make[Hasher[F]].from[Hasher.BCryptImpl[F]]

    make[AuthService[F]]
    make[Tokens[F]].from[JwtTokens[F]]
    make[UserRepository[F]].from[UserRepository.InMemoryImpl[F]]
    make[CreateJwtTokenEndpoint[F]]

    many[ServerEndpoint[Any, F]]
      .add((e: CreateJwtTokenEndpoint[F]) => e.route)

    many[RequestHandlerBase[F]]
    many[NotificationHandlerBase[F]]
  }

  val endpoints: Endpoints = List(
    CreateJwtTokenEndpoint.route,
  )
}
