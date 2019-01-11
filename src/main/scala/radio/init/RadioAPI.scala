package radio.init

import akka.NotUsed
import akka.stream.scaladsl.{Keep, Sink, Source, Tcp}
import akka.util.ByteString
import radio.v0.ID

import scala.concurrent.{Future, Promise}

case class RadioAPIImpl(config: Map[String,String])
//
trait RadioAPITCP extends RadioAPIImpl
//object RadioAPITCP extends RadioAPITCP
//
trait RadioAPIFS extends RadioAPIImpl
//object RadioAPIFS extends RadioAPIFS

trait RadioAPI[T] {
  def streamMedia(episodeID: ID, t:T): Future[(Long,Source[ByteString,NotUsed])]
}

object RadioAPI {
  import RadioDomain._

  implicit object RadioAPITCP extends RadioAPI[RadioAPITCP] {

    def streamMedia(episodeID: ID, tcp: RadioAPITCP) = {
      import tcp._

//      val connection = Tcp().outgoingConnection(config("host"), config("port").toInt)
      //      val fileLengthReqPayload = Source.single(ByteString(s"length-${episodeID.toString}"))
      //        .concat(Source.single(ByteString(System.lineSeparator())))
      //      val fileContentReqPayload = Source.single(ByteString(episodeID.toString))
      //        .concat(Source.single(ByteString(System.lineSeparator())))
      //
      //      val fileLengthReqFlow = fileLengthReqPayload.via(connection)
      //      val fileContentReqFlow = fileContentReqPayload.via(connection)
      //
      //
      //      val f: Future[ByteString] = fileLengthReqFlow.toMat(Sink.head[ByteString])(Keep.right).run()
      //
      //      val promise = Promise[(Long, Source[ByteString, NotUsed])]()
      //
      //      f onSuccess {
      //        case l: ByteString =>
      //          promise success(l.utf8String.toLong, fileContentReqFlow)
      //      }
      //
      //      promise.future

      Future.successful((0L, Source.single(ByteString(""))))
    }

  }

//  def streamMedia(episodeID: ID) = {
//
//    val host = "127.0.0.1"
//    // TODO: get it from config
//    val port = 2828 // TODO: get it from config
//
//    val connection = Tcp().outgoingConnection(host, port)
//    val fileLengthReqPayload = Source.single(ByteString(s"length-${episodeID.toString}"))
//      .concat(Source.single(ByteString(System.lineSeparator())))
//    val fileContentReqPayload = Source.single(ByteString(episodeID.toString))
//      .concat(Source.single(ByteString(System.lineSeparator())))
//
//    val fileLengthReqFlow = fileLengthReqPayload.via(connection)
//    val fileContentReqFlow = fileContentReqPayload.via(connection)
//
//
//    val f: Future[ByteString] = fileLengthReqFlow.toMat(Sink.head[ByteString])(Keep.right).run()
//
//    val promise = Promise[(Long, Source[ByteString, NotUsed])]()
//
//    f onSuccess {
//      case l: ByteString =>
//        promise success(l.utf8String.toLong, fileContentReqFlow)
//    }
//
//    promise.future
//  }
}
