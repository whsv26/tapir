package org.whsv26.tapir
package domain.foos

import infrastructure.endpoint.foos.CreateFooEndpoint.CreateFoo

trait FooRepositoryAlg[F[_]] {
  def findById(id: FooId): F[Option[Foo]]

  def create(id: FooId, foo: CreateFoo): F[Foo]

  def delete(id: FooId): F[Unit]
}
