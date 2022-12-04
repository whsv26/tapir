package org.whsv26.tapir

import util.http.security.{Endpoints, ServerEndpoints}

import cats.effect.kernel.Sync
import org.whsv26.tapir.auth.AuthService

package object auth {
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
