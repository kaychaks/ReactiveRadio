package radio.v0
package repository

import cats.data.Xor
import radio.v0.domain.customexceptions.CustomException.ContentEncodingDecodingError

sealed trait MetadataEncoder[C,T]{
  def encode(content: C): Xor[ContentEncodingDecodingError,T]
}

sealed trait MetadataDecoder[C,T] extends CanCreateContentX[T,C]{
  def decode(value:T):Xor[ContentEncodingDecodingError,C]
  override def apply(f: T) = decode(f)
}

trait ContentMetadataEncoderDecoder[C, T] extends MetadataEncoder[C,T] with MetadataDecoder[C,T]
