package org.whsv26.tapir
package application.endpoint

import application.security.{Endpoints, ServerEndpoints}
import domain.auth.AuthService

import cats.effect.kernel.Sync

package object jwt {
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
