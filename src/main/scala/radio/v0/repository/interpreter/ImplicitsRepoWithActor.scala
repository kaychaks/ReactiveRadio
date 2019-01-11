package radio.v0
package repository
package interpreter

import akka.actor.ActorSystem
import akka.event.{Logging, LoggingAdapter}
import akka.stream.ActorMaterializer

import scala.concurrent.ExecutionContext

object ImplicitsRepoWithActor extends ImplicitsRepo {
  implicit val actorSystem: ActorSystem = ActorSystem("application-actor-system")
  implicit val executionContext: ExecutionContext = actorSystem.dispatcher
  implicit val actorMaterializer: ActorMaterializer = ActorMaterializer()
  implicit val log: LoggingAdapter = Logging(actorSystem, "string-source")
}
