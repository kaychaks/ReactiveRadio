package radio.init

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.{Connection, EntityTag}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route

sealed trait Routes[T] {

  def showsAndEpisodesFetchRoutes = {
    get {
      pathPrefix("shows") {
        pathEnd {
          // e.g. /shows
          ???
        } ~
          path(Segment) { showName =>
            pathEnd {
              // e.g. /shows/thetechshow
              ???
            } ~
              path("episodes") {
                pathEnd {
                  // e.g. /shows/thetechshow/episodes
                  ???
                } ~
                  path(IntNumber) { episode =>
                    // e.g. /shows/thetechshow/episodes/71
                    ???
                  }
              }
          }
      }
    }
  }

  def showsAndEpisodesPostRoutes = {
    (post | put | delete) {
      extractMethod { method =>
        pathPrefix("shows" / Segment) { showName =>
          pathEnd {
            // e.g. POST /shows/thetechshow (create a new show)
            ???
          } ~
            path("episodes") {
              pathEnd {
                // e.g. POST /shows/thetechshow/episodes (uploading a new episode)
                ???
              } ~
                path(IntNumber) { episode =>
                  // e.g. DELETE /shows/thetechshow/episodes/71
                  ???
                }
            }
        }
      }
    }
  }

  // TODO: get it from config
  val mediaDirectoryPath = "/mnt/uploads"

  def staticFileRoutes(implicit api:RadioAPI[T]) = {
    pathPrefix("shows" / Segment / "media" / Segment) { (showName, episode) =>
      // e.g. GET /shows/thetechshow/media/episode-71.mp3 --> /mnt/uploads/thetechshow/episode-71.mp3
      //getFromDirectory(s"$mediaDirectoryPath/$showName")
      respondWithHeader(Connection("keep-alive")) {
        conditional(EntityTag(s"some-entity-$showName-$episode"), DateTime(2015, 1, 1)) {
          withRangeSupport {
            complete {
              HttpResponse(StatusCodes.OK)
            }
//            onSuccess(api.streamMedia(episode)) {
//              case (l, content) =>
//                complete {
//                  HttpEntity.Default(
//                    ContentType(MediaTypes.`audio/mpeg`),
//                    l,
//                    content
//                  )
//                }
//            }
          }
        }
      }
    }
  }
}

final case class RestAPI[T] private(implicit val actorSystem: ActorSystem,
                                 val log: LoggingAdapter) extends Routes[T] {


  def routes: Route = redirectToNoTrailingSlashIfPresent(Found) {
//    showsAndEpisodesFetchRoutes ~ showsAndEpisodesPostRoutes ~ staticFileRoutes
    showsAndEpisodesFetchRoutes
  }

}

object RestAPI {
//  def create(implicit system: ActorSystem, log: LoggingAdapter): RestAPI = {
//    new RestAPI
//  }
}





