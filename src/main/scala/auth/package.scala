package org.whsv26.tapir

import util.http.security.{Endpoints, ServerEndpoints}

import cats.effect.kernel.Sync
import distage.{Tag, TagK}
import izumi.distage.model.definition.ModuleDef

package object auth {
  def module[F[_]: TagK] = new ModuleDef {
    make[AuthService[F]]
    make[CreateJwtTokenEndpoint[F]]
    make[Hasher[F]].from[BCryptHasher[F]]
    make[JwtClock[F]].from[JwtClock.SystemImpl[F]]
    make[Tokens[F]].from[JwtTokens[F]]
    make[UserRepository[F]].from[InMemoryUserRepository[F]]
  }

  def serverEndpoints[F[_]: Sync](
    auth: AuthService[F]
  ): ServerEndpoints[F] =
    List(
      new CreateJwtTokenEndpoint(auth).route,
    )

  val endpoints: Endpoints = List(
    CreateJwtTokenEndpoint.route,
  )
}
