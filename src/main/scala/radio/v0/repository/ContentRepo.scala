package radio.v0
package repository

import cats.data.{Xor, XorT}
import radio.init.RadioDomain.ID
import radio.v0.domain.customexceptions.CustomException

import scala.concurrent.{ExecutionContext, Future}

trait CanCreateContent[-From,+To]{
  def apply(f: From): To
}
trait CanCreateContentX[F,T] extends CanCreateContent[F,Xor[CustomException,T]]

trait ContentRepo{

  import cats.implicits._

  def store[T](c: T): XorT[Future, CustomException, T]

  def get[F,T](c: ID)(implicit cb:CanCreateContent[F,T]): XorT[Future, CustomException, T]

  def delete[T](c: T): XorT[Future, CustomException, ID]

  def generateID(
    implicit
      ec: ExecutionContext
  ): XorT[Future, CustomException, ID] = {
    Xor
      .catchNonFatal[ID] {
      UUID.randomUUID.toString
    }
      .leftMap[CustomException](ex => ErrorGeneratingID(Option(ex.getMessage)))
      .toXorT[Future]
  }
}
