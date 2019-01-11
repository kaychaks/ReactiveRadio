package radio.v0
package repository
package interpreter

import java.io.File
import java.net.URL
import java.nio.file.{Files, Path, Paths}
import java.time.LocalDate

import akka.Done
import akka.stream.IOResult
import akka.stream.scaladsl.{FileIO, Flow, Sink, Source}
import akka.util.ByteString
import cats.Apply
import cats.data.{Xor, XorT}
import radio.init.RadioDomain.ID
import radio.v0.domain.{Content, _}
import radio.v0.domain.customexceptions.CustomException
import radio.v0.domain.customexceptions.CustomException.{ContentEncodingDecodingError, ContentNotFound, ErrorStoringContent}
import spray.json.{JsValue, _}

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}




object ContentEncoders {
  implicit val networkEncDec = new ContentMetadataEncoderDecoder[Network,JsValue] {
    import JSONProtocols._

    override def encode(content: Network): Xor[ContentEncodingDecodingError,JsValue] = Try {
      content.toJson
    }  match {
      case Success(j:JsValue) => Xor.right(j)
      case Failure(e) => Xor.left(ContentEncodingDecodingError(Some(e.getMessage)))
    }

    override def decode(value: JsValue): Xor[ContentEncodingDecodingError, Network] = Try {
      value.convertTo[Network]
    } match {
      case Success(n:Network) => Xor.right(n)
      case Failure(e) => Xor.left(ContentEncodingDecodingError(Some(e.getMessage)))
    }
  }
  implicit val showEncDec = new ContentMetadataEncoderDecoder[Show, JsValue] {
    override def decode(value: JsValue): Xor[ContentEncodingDecodingError, Show] = ???

    override def encode(content: Show): Xor[ContentEncodingDecodingError, JsValue] = ???
  }
  implicit val episodeEncDec = new ContentMetadataEncoderDecoder[Episode, JsValue] {
    override def decode(value: JsValue): Xor[ContentEncodingDecodingError, Episode] = ???

    override def encode(content: Episode): Xor[ContentEncodingDecodingError, JsValue] = ???
  }
}

sealed trait ContentTypeFetcher[-T]{
  def getType: String
}
object ContentTypeFetcher{
  implicit val networkType = new ContentTypeFetcher[Network] {override def getType = "NETWORK"}
  implicit val showType = new ContentTypeFetcher[Show] {override def getType = "SHOW"}
  implicit val episodeType = new ContentTypeFetcher[Episode] {override def getType = "EPISODE"}
  implicit val noneType = new ContentTypeFetcher[String] {override def getType = ""}
}

trait ContentRepoFileSystem extends ContentRepo {

  import ConfigurationRepoInterpretor._
  import ImplicitsRepoWithActor._
  import cats.data._
  import cats.implicits._
  import ContentTypeFetcher._

  implicit val networkEncoder = new ContentMetadataEncoderDecoder[Network,JsValue] {
    import JSONProtocols._

    override def encode(content: Network): Xor[ContentEncodingDecodingError,JsValue] = Try {
      content.toJson
    }  match {
      case Success(j:JsValue) => Xor.right(j)
      case Failure(e) => Xor.left(ContentEncodingDecodingError(Some(e.getMessage)))
    }

    override def decode(value: JsValue): Xor[ContentEncodingDecodingError, Network] = Try {
      value.convertTo[Network]
    } match {
      case Success(n:Network) => Xor.right(n)
      case Failure(e) => Xor.left(ContentEncodingDecodingError(Some(e.getMessage)))
    }
  }
  implicit val showEncoder = new ContentMetadataEncoderDecoder[Show, JsValue] {
    override def decode(value: JsValue): Xor[ContentEncodingDecodingError, Show] = ???

    override def encode(content: Show): Xor[ContentEncodingDecodingError, JsValue] = ???
  }
  implicit val episodeEncoder = new ContentMetadataEncoderDecoder[Episode, JsValue] {
    override def decode(value: JsValue): Xor[ContentEncodingDecodingError, Episode] = ???

    override def encode(content: Episode): Xor[ContentEncodingDecodingError, JsValue] = ???
  }

  type CMED[T] = ContentMetadataEncoderDecoder[T,JsValue]
  def encodeContent[T <: Content : CMED](c:T): Xor[CustomException,JsValue] = implicitly[CMED[T]].encode(c)
  def decodeContent[T <: Content: CMED](c:JsValue): Xor[CustomException,T] = implicitly[CMED[T]].decode(c)


  private def generateFileName[T <: Content](id: ID, title: String)(implicit ct: ContentTypeFetcher[T]): Path =
    Paths.get(config.getString("application.storage.directory"),
      s"$id-${ct.getType}-${title.take(7).replaceAll("\\s","_")}"
    )

  private def searchContentInFS(id: ID): Option[File] =
    Paths.get(config.getString("application.storage.directory")).toFile.listFiles
    .find(_.getName.startsWith(s"$id-"))

  override def store[T](c: T): XorT[Future, CustomException, T] = ???
//  {
//    type XoS[A] = Xor[CustomException,A]
//    def enOp: XoS[JsValue] = c match {
//      case n: Network => encodeContent(n)
//      case e: Episode => encodeContent(e)
//      case s : Show => encodeContent(s)
//    }
//
//    def fn: XoS[Path] = c match {
//      case n: Network => generateFileName[Network](n.id,n.title).right[CustomException]
//      case e: Episode => generateFileName[Episode](e.id,e.title).right[CustomException]
//      case s: Show => generateFileName[Show](s.id,s.title).right[CustomException]
//    }
//
//    val st = enOp |@| fn map {
//      (en, p) =>
//        Source.single(ByteString(en.compactPrint)).runWith(FileIO.toPath(p))
//    }
//
//    st fold (c => c.left[T].toXorT[Future] ,
//      ft => XorT[Future,CustomException,T] {
//        for {
//          io <- ft
//        } yield io.status match {
//          case Success(Done) => c.right[CustomException]
//          case Failure(e) => ErrorStoringContent(Some(e.getMessage)).left[T]
//        }
//      }
//    )
//  }

  override def get[F,T](c: ID)(implicit cd: CanCreateContent[F,T]): XorT[Future, CustomException, T] = ???
//  {
//    val parsedJSON: Option[F] = for {
//      file <- searchContentInFS(c)
//      str <- Files.readAllLines(Paths.get(file.getAbsolutePath)).toArray(Array("")).headOption
//    } yield str.parseJson
//
//    parsedJSON.map(js => cd(js))
//      .getOrElse(ContentNotFound().left[T])
//      .toXorT[Future]
//  }

  override def delete[T](c: T): XorT[Future, CustomException, ID] = ???
}

object ContentRepoFileSystem extends ContentRepoFileSystem
