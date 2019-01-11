package radio.init

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.stream.ActorMaterializer

import scala.concurrent.{ExecutionContext, Future}
import scala.io.StdIn

/**
  * Created by kaushik on 06/09/16.
  */
object Main extends App {

  implicit val actorSystem = ActorSystem("reactive-radio-control-system")
  implicit val ec: ExecutionContext = actorSystem.dispatcher
  implicit val log = Logging(actorSystem.eventStream, "reactive-radio")
  implicit val config = Map(
    "host" -> "127.0.0.1",
    "port" -> "2288"
  )

//  implicit val radioAPITCP = RadioAPITCP

//  val api = RestAPI.create.routes
//
//  implicit val materializer: ActorMaterializer = ActorMaterializer()
//  val bindingFuture: Future[ServerBinding] = {
//    // TODO: get things from config
//    Http().bindAndHandle(api, "127.0.0.1", 8080) //Starts the HTTP server
//  }
//
//
//  bindingFuture.map { serverBinding =>
//    log.info(s"RestApi bound to ${serverBinding.localAddress} ")
//  }.onFailure {
//    case ex: Exception =>
//      log.error(ex, "Failed to bind to {}:{}!", "127.0.0.1", 8080)
//      actorSystem.terminate()
//  }

  StdIn.readLine()

  actorSystem.terminate()
}
