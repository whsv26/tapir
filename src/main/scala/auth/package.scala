package org.whsv26.tapir

import util.http.security.{Endpoints, ServerEndpoints}

import cats.effect.kernel.Sync
import distage.{Tag, TagK}
import izumi.distage.model.definition.ModuleDef

package object auth {
  def authModule[F[_]: TagK] = new ModuleDef {
    make[HasherAlg[F]].from[BCryptHasherAlgInterpreter[F]]
    make[AuthService[F]]
    make[CreateJwtTokenEndpoint[F]]
    make[JwtClockAlg[F]].from[JwtClockAlg.SystemClockImpl[F]]
    make[TokenAlg[F]].from[JwtTokenAlgInterpreter[F]]
    make[UserRepositoryAlg[F]].from[MemUserRepositoryAlgInterpreter[F]]
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
