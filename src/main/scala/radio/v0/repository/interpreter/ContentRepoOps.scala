package radio.v0.repository.interpreter

import cats.data.Xor
import radio.v0.domain.{Episode, Network, Show}
import radio.v0.domain.customexceptions.CustomException
import radio.v0.repository.RepoOps
import spray.json.JsValue

object ContentRepoOps {
  import JsonOps._
  import JSONProtocols._

  implicit val networkOps = new RepoOps[Network, JsValue] {
    val ct: ContentTypeFetcher[Network] = ContentTypeFetcher.networkType

    override def stringToT(s: String): Xor[CustomException, JsValue] =
      jsonStringToT(s)

    override def show(t: JsValue): String = jsonShow(t)

    override def encode(a: Network): Xor[CustomException, JsValue] =
      jsonEncode[Network](a)

    override def decode(t: JsValue): Xor[CustomException, Network] =
      jsonDecode[Network](t)
  }

  implicit val showOps = new RepoOps[Show, JsValue] {
    val ct: ContentTypeFetcher[Show] = ContentTypeFetcher.showType

    override def stringToT(s: String): Xor[CustomException, JsValue] =
      jsonStringToT(s)

    override def show(t: JsValue): String = jsonShow(t)

    override def encode(a: Show): Xor[CustomException, JsValue] =
      jsonEncode[Show](a)

    override def decode(t: JsValue): Xor[CustomException, Show] =
      jsonDecode[Show](t)
  }

  implicit val episodeOps = new RepoOps[Episode, JsValue] {
    val ct: ContentTypeFetcher[Episode] = ContentTypeFetcher.episodeType

    override def stringToT(s: String): Xor[CustomException, JsValue] =
      jsonStringToT(s)

    override def show(t: JsValue): String = jsonShow(t)

    override def encode(a: Episode): Xor[CustomException, JsValue] =
      jsonEncode[Episode](a)

    override def decode(t: JsValue): Xor[CustomException, Episode] =
      jsonDecode[Episode](t)
  }
}