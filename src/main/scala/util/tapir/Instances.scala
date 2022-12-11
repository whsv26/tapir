package org.whsv26.tapir
package util.tapir

import io.estatico.newtype.Coercible

trait Instances {
  implicit def newTypeParamCodec[A, B](implicit
    codec: ParamCodec[B],
    atoB: Coercible[A, B],
    bToA: Coercible[B, A],
  ): ParamCodec[A] =
    codec.map(b => bToA(b))(a => atoB(a))
}
