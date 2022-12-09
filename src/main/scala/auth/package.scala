package org.whsv26.tapir

import util.bus.{NotificationHandlerBase, RequestHandlerBase}
import util.http.security.Endpoints

import distage.TagK
import izumi.distage.model.definition.ModuleDef
import sttp.tapir.server.ServerEndpoint

package object auth {
  def module[F[_]: TagK] = new ModuleDef {
    make[AuthService[F]]
    make[CreateJwtTokenEndpoint[F]]
    make[Hasher[F]].from[BCryptHasher[F]]
    make[JwtClock[F]].from[JwtClock.SystemImpl[F]]
    make[Tokens[F]].from[JwtTokens[F]]
    make[UserRepository[F]].from[InMemoryUserRepository[F]]

    many[ServerEndpoint[Any, F]]
      .add((e: CreateJwtTokenEndpoint[F]) => e.route)

    many[RequestHandlerBase[F]]
    many[NotificationHandlerBase[F]]
  }

  val endpoints: Endpoints = List(
    CreateJwtTokenEndpoint.route,
  )
}
