package radio.v0
package repository

import akka.actor.ActorSystem
import akka.event.LoggingAdapter

import scala.concurrent.ExecutionContext

trait ImplicitsRepo {

  implicit val actorSystem: ActorSystem
  implicit val executionContext : ExecutionContext
//  implicit val actorMaterializer : ActorMaterializer
  implicit val log: LoggingAdapter

}
