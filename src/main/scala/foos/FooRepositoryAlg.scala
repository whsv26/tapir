package org.whsv26.tapir
package foos

trait FooRepositoryAlg[F[_]] {
  def findById(id: Foo.Id): F[Option[Foo]]

  def create(foo: Foo): F[Int]

  def update(foo: Foo): F[Int]

  def delete(id: Foo.Id): F[Unit]
}
