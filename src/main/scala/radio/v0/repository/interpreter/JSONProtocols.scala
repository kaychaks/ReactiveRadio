package radio.v0.repository.interpreter

import java.net.URL
import java.nio.file.{Path, Paths}
import java.time.{Duration, LocalDate, LocalDateTime}

import radio.v0.domain._
import spray.json._

object JSONProtocols extends DefaultJsonProtocol with NullOptions {

  implicit def urlJSONFormat = new JsonFormat[URL] {
    override def write(obj: URL): JsValue = JsString(obj.toString)

    override def read(json: JsValue): URL = json match {
      case JsString(value) => new URL(value)
      case _ => deserializationError("URL expected")
    }
  }

  implicit def localDateJSONFormat = new JsonFormat[LocalDate] {
    override def write(obj: LocalDate): JsValue = JsString(obj.toString)

    override def read(json: JsValue): LocalDate = json match {
      case JsString(value) => LocalDate.parse(value)
      case _ => deserializationError("LocalDate expected")
    }
  }

  implicit def localDateTimeJSONFormat = new JsonFormat[LocalDateTime] {
    override def write(obj: LocalDateTime): JsValue = JsString(obj.toString)

    override def read(json: JsValue): LocalDateTime = json match {
      case JsString(value) => LocalDateTime.parse(value)
      case _ => deserializationError("LocalDateTime expected")
    }
  }

  implicit def durationJSONFormat = new JsonFormat[Duration] {
    override def write(obj: Duration): JsValue = JsString(obj.toString)

    override def read(json: JsValue): Duration = json match {
      case JsString(value) => Duration.parse(value)
      case _ => deserializationError("Duration expected")
    }
  }

  implicit def pathJSONFormat = new JsonFormat[Path] {
    override def write(obj: Path): JsValue = JsString(obj.toString)

    override def read(json: JsValue): Path = json match {
      case JsString(value) => Paths.get(value)
      case _ => deserializationError("Path expected")
    }
  }


  implicit val hostJSONFormat: JsonFormat[Host] = jsonFormat2(Host.apply)
  implicit val feedJSONFormat: JsonFormat[Feed] = jsonFormat3(Feed.apply)
  implicit val mediaJSONFormat: JsonFormat[Media] = jsonFormat4(Media.apply)
  implicit val tagJSONFormat: JsonFormat[Tag] = jsonFormat4(Tag.apply)
  implicit val networkJSONFormat: JsonFormat[Network] = jsonFormat6(
    Network.apply)
  implicit val showJSONFormat: JsonFormat[Show] = jsonFormat9(Show.apply)
  implicit val episodeJSONFormat: JsonFormat[Episode] = jsonFormat10(
    Episode.apply)
}
