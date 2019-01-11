package radio.init

import java.nio.file.{InvalidPathException, Paths}

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.stream.scaladsl.Tcp.{IncomingConnection, ServerBinding}
import akka.stream.scaladsl.{Broadcast, FileIO, Flow, Framing, GraphDSL, Merge, Sink, Source, Tcp}
import akka.stream.{ActorAttributes, ActorMaterializer, FlowShape, Supervision}
import akka.util.ByteString

import scala.concurrent.{ExecutionContext, Future}

object MediaManager {

  type SocketConnectionSource = Source[IncomingConnection, Future[ServerBinding]]

  def props(implicit materializer: ActorMaterializer, system: ActorSystem): Props = Props(new MediaManager)

  def name = "MediaManager"

  case object Start

  case class Stop(source: SocketConnectionSource)

}

class MediaManager(implicit val materializer: ActorMaterializer,
                   val system: ActorSystem) extends Actor with ActorLogging {

  import MediaManager._

  implicit val ec: ExecutionContext = system.dispatcher

  val exceptionHandler: Supervision.Decider = {
    case ex: InvalidPathException =>
      log.error("FILE PATH ERROR : {}", ex.getMessage)
      self ! Stop // Kill the connection
      Supervision.Stop // Kill the stream
    case e =>
      log.error("ERROR: {}", e.getMessage)
      self ! Stop
      Supervision.Stop
  }

  override def receive: Receive = {
    case Start =>

      val host = "127.0.0.1"
      // TODO: get it from config
      val port = 2828
      // TODO: get it from config
      val connections: SocketConnectionSource = Tcp().bind(host, port)

      def lengthPredicate(x: String): Boolean = x.take(7) == "length-"

      def checkForLength = Flow[ByteString]
        .map(_.utf8String)
        .filter(lengthPredicate)
        .map(_.drop(7))
        .map(x => Paths.get(s"/Users/kaushik/Music/$x.mp3").toFile.length())
        .map(x => ByteString(x.toString))


      def checkForContent = Flow[ByteString]
        .map(_.utf8String)
        .filterNot(lengthPredicate)
        .map(x => Paths.get(s"/Users/kaushik/Music/$x.mp3"))
        .flatMapConcat(p => FileIO.fromPath(p))

      val g = GraphDSL.create() { implicit b =>

        import GraphDSL.Implicits._

        val bcast = b.add(Broadcast[ByteString](2))
        val merge = b.add(Merge[ByteString](2, eagerComplete = false))

        bcast.out(0) ~> checkForLength ~> merge.in(0)
        bcast.out(1) ~> checkForContent ~> merge.in(1)

        FlowShape(bcast.in, merge.out)
      }

      connections runForeach  { conn =>

        log.info("TCP Connection opened from {}", conn.remoteAddress)

        val fetchFlow =
          Flow[ByteString]
            .via(Framing.delimiter(ByteString(System.lineSeparator()), 256, allowTruncation = true))
            .via(g)
            .withAttributes(ActorAttributes.supervisionStrategy(exceptionHandler))


        conn.handleWith(fetchFlow)
      }

      sender() ! connections

    case Stop(source: SocketConnectionSource) =>
      (source to Sink.cancelled).run() onComplete (_ => log.info("TCP Connection Closed"))
  }
}
