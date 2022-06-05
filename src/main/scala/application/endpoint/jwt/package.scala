package org.whsv26.tapir
package application.endpoint

import domain.auth.AuthService

import cats.effect.kernel.Sync
import sttp.tapir.server.ServerEndpoint

package object jwt {
  def routes[F[_]: Sync](
    auth: AuthService[F]
  ): List[ServerEndpoint[Any, F]] =
    List(
      new CreateJwtTokenEndpoint(auth).route,
    )

  val endpoints = List(
    CreateJwtTokenEndpoint.route,
  )
}
