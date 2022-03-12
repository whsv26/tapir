package org.whsv26.tapir
package domain.auth

import domain.users.Users.UserId

trait JwtTokenAlgebra[F[_]] {
  def generateToken(id: UserId): F[JwtToken]
}
