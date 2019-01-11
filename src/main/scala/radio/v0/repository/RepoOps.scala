package radio.v0.repository

import cats.data.Xor
import radio.v0.domain.customexceptions.CustomException
import radio.v0.repository.interpreter.ContentTypeFetcher

trait RepoOps[A,T] {
  def ct: ContentTypeFetcher[A]
  def encode(a: A) : CustomException Xor T
  def decode(t: T) : CustomException Xor A
  def stringToT(s: String): CustomException Xor T
  def decode(s : String) : CustomException Xor A = for {
    t <- stringToT(s)
    a <- decode(t)
  } yield a
  def show(t: T): String
}
