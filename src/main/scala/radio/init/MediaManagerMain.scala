package radio.init

import java.util.concurrent.TimeUnit

import akka.actor.{ActorRef, ActorSystem}
import akka.event.Logging
import akka.stream.ActorMaterializer
import akka.util.Timeout
import radio.init.MediaManager.{SocketConnectionSource, Start}

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ExecutionContext, Future}
import scala.io.StdIn

object MediaManagerMain extends App {

  import akka.pattern.ask

  implicit val system = ActorSystem("mediamanager-system")
  implicit val ec: ExecutionContext = system.dispatcher
  implicit val materializer = ActorMaterializer()
  implicit val timeout: Timeout = FiniteDuration(30,TimeUnit.SECONDS)
  val actor: ActorRef = system.actorOf(MediaManager.props, MediaManager.name)

  val log = Logging(system.eventStream, "mediamanager-system")
  val tcpStartFuture: Future[SocketConnectionSource] = actor.ask(Start).mapTo[SocketConnectionSource]
  tcpStartFuture.map { conn =>
    log.info("TCP Server Started")
  }.onFailure {
    case ex: Exception =>
      log.error(ex, "TCP Server Start Failed")
      system.terminate()
  }

  StdIn.readLine()
  system.terminate()
}
