package org.whsv26.tapir
package util

import sttp.tapir.{Codec, CodecFormat}

package object tapir extends Instances {
  // path|query|header parameter codec
  type ParamCodec[T] = Codec[String, T, CodecFormat.TextPlain]
}
