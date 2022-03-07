package org.whsv26.tapir

import Foo.FooId

trait FooRepositoryAlgebra[F[_]] {
  def findById(id: FooId): F[Option[Foo]]
  def create(foo: Foo): F[FooId]
  def delete(id: FooId): F[Int]
}
