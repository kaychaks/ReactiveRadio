package radio.init

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.stream.ActorMaterializer

import scala.concurrent.ExecutionContext

case class Boilerplate(implicit val actorSystem: ActorSystem, val log: LoggingAdapter) {
  implicit val materializer: ActorMaterializer = ActorMaterializer ()
  implicit val executionContext: ExecutionContext = actorSystem.dispatcher
}
