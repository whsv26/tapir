package org.whsv26.tapir

import Foo.FooId

trait FooRepositoryAlgebra[F[_]] {
  def findById(id: FooId): F[Option[Foo]]
  def create(id: FooId, foo: CreateFoo): F[Foo]
  def delete(id: FooId): F[Unit]
}
