package radio.v0.repository.interpreter

import cats.data.Xor
import radio.v0.domain.customexceptions.CustomException
import radio.v0.domain.customexceptions.CustomException.ContentEncodingDecodingError

import scala.util.{Failure, Success, Try}

object JsonOps {
  import spray.json._

  def jsonShow(t: JsValue): String = t.compactPrint
  def jsonStringToT(s: String): Xor[CustomException, JsValue] =
    Xor.catchNonFatal(s.parseJson).leftMap(ex => ContentEncodingDecodingError(Some(ex.getMessage)))
  def jsonEncode[A : JsonWriter](a: A): Xor[CustomException, JsValue] = {
    Try {
      a.toJson
    }  match {
      case Success(j:JsValue) => Xor.right(j)
      case Failure(e) => Xor.left(ContentEncodingDecodingError(Some(e.getMessage)))
    }
  }
  def jsonDecode[A : JsonReader](t: JsValue): Xor[CustomException, A] = {
    Try {
      t.convertTo[A]
    } match {
      case Success(n) => Xor.right(n)
      case Failure(e) => Xor.left(ContentEncodingDecodingError(Some(e.getMessage)))
    }
  }
}


