package org.whsv26.tapir
package domain.foos

trait FooRepositoryAlg[F[_]] {
  def findById(id: FooId): F[Option[Foo]]

  def create(foo: Foo): F[Int]

  def update(foo: Foo): F[Int]

  def delete(id: FooId): F[Unit]
}
