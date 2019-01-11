package radio.v0
package service
package interpreter

import java.net.URL
import java.time.{Duration, LocalDate, LocalDateTime}

import cats.data.Kleisli
import cats.implicits._
import radio.v0.domain._
import radio.v0.repository.interpreter.ContentRepoOps._
import radio.v0.repository.interpreter.ImplicitsRepoWithActor._
import radio.v0.repository.{Repo, RepoOps}
import spray.json.JsValue

import scala.concurrent.Future

trait NetworkServiceInterpreter extends NetworkService[Network, Show] {
  override def createNetwork(
      title: String,
      desc: Option[String],
      creationDate: Option[LocalDate]): ServiceOperation[Network] =
    Kleisli[Future, RepoContent, ValidData[Network]] {
      repo: Repo[Network, RepoOps] =>
        {
          for {
            id <- Content.generateID
            n = Network(
              id,
              title,
              desc,
              creationDate.fold(Some(LocalDate.now()))(Some(_)),
              Vector.empty,
              Vector.empty
            )
            _ <- repo.store[Network, JsValue](n)
          } yield n
        }.toValidatedNel
    }

  override def listShows(n: Network): ServiceOperation[Vector[Show]] =
    Kleisli[Future, RepoContent, ValidData[Vector[Show]]] {
      repo: Repo[Show, RepoOps] =>
        n.showIds
          .traverse[FutureValueOrCustomException, Show](
            repo.get[Show, JsValue])
          .toValidatedNel
    }

  override def findNetwork(id: ID): ServiceOperation[Network] =
    Kleisli[Future, RepoContent, ValidData[Network]] {
      repo: Repo[Network, RepoOps] =>
        repo.get[Network, JsValue](id).toValidatedNel
    }
}

trait ShowServiceInterpreter extends ShowService[Show, Network, Episode] {

  override def createShow(title: String,
                          creationDate: Option[LocalDate],
                          hostNameEmails: Vector[(String, String)],
                          description: Option[String],
                          albumArt: Option[URL],
                          networkId: Option[ID]): ServiceOperation[Show] =
    Kleisli[Future, RepoContent, ValidData[Show]] {
      repo: Repo[Show, RepoOps] =>
        {
          lazy val hostsF = hostNameEmails
            .traverse[FutureValueOrCustomException, (ID, (String, String))] {
              ne: (String, String) =>
                for {
                  id <- Content.generateID
                } yield id -> ne
            }

          for {
            id <- Content.generateID
            hostVec <- hostsF
            s = Show(id,
                     title,
                     creationDate.fold(Some(LocalDate.now()))(Some(_)),
                     Vector.empty, //todo
                     Vector.empty,
                     description,
                     albumArt,
                     Vector.empty,
                     networkId)
            _ <- repo.store[Show, JsValue](s)
          } yield s
        }.toValidatedNel
    }

  override def addShowToNetwork(s: Show, n: Network): ServiceOperation[Show] =
    Kleisli[Future, RepoContent, ValidData[Show]] {
      repo: Repo[Show, RepoOps] =>
        repo
          .store[Show, JsValue](s.copy(networkId = Option(n.id)))
          .toValidatedNel
    }

  override def listEpisodesForAShow(
      s: Show): ServiceOperation[Vector[Episode]] =
    Kleisli[Future, RepoContent, ValidData[Vector[Episode]]] {
      repo: Repo[Episode, RepoOps] =>
        s.episodeIds
          .traverse[FutureValueOrCustomException, Episode](
            repo.get[Episode, JsValue])
          .toValidatedNel
    }

  override def listEpisodesForShows(
      s: Vector[Show]): ServiceOperation[Vector[Episode]] = {
    s.traverseU(listEpisodesForAShow).map {
      f: Vector[ValidData[Vector[Episode]]] =>
        f.sequenceU.map { x: Vector[Vector[Episode]] =>
          x.flatten
        }
    }
  }

  override def findShow(id: ID): ServiceOperation[Show] =
    Kleisli[Future, RepoContent, ValidData[Show]] {
      repo: Repo[Show, RepoOps] =>
        repo.get[Show, JsValue](id).toValidatedNel
    }
}

trait EpisodeServiceInterpreter
    extends EpisodeService[Episode, Show, Media, Tag] {
  override def createEpisode(title: String,
                             creationDate: Option[LocalDate],
                             description: Option[String],
                             albumArt: Option[URL],
                             duration: Option[Duration],
                             showNotes: Option[String],
                             recordingDate: Option[LocalDateTime],
                             hashtags: Seq[String],
                             mediaFilePath: Option[String],
                             showId: Option[ID],
                             show: Option[Show]): ServiceOperation[Episode] =
//    Kleisli[Future, Repo, ValidData[Episode]] {
//      repo: ContentRepo1[Episode, JsValue] =>
//        for {
//          id <- Content.generateID
//          episode = Episode(id, title, description, creationDate,
//            recordingDate, showNotes, None, albumArt,
//            Vector.empty, showId)
//        }
//
//    }
    ???

  override def addEpisodeToShow(e: Episode,
                                s: Show): ServiceOperation[Episode] = ???

  override def addMediaToEpisode(mediaFileURL: String,
                                 e: Episode): ServiceOperation[Episode] = ???

  override def addTagsToEpisode(t: Seq[Tag],
                                e: Episode): ServiceOperation[Episode] = ???

  override def removeTagsFromEpisode(t: Seq[Tag],
                                     e: Episode): ServiceOperation[Episode] =
    ???

  override def updateShowNotes(notes: String,
                               e: Episode): ServiceOperation[Episode] = ???

  override def updateMedia(media: Media,
                           e: Episode): ServiceOperation[Episode] = ???

  override def findEpisode(id: ID): ServiceOperation[Episode] = ???
}

object ContentService
    extends NetworkServiceInterpreter
    with ShowServiceInterpreter
    with EpisodeServiceInterpreter {

  def listEpisodes(n: Network): ServiceOperation[Vector[Episode]] = {
    for {
      validShows <- listShows(n)
      episode <- listEpisodesForShows(
        validShows.toOption.getOrElse(Vector.empty))
    } yield episode
  }

  def listEpisodes(fc: FeedableContent): ServiceOperation[Vector[Episode]] =
    fc match {
      case n: Network => listEpisodes(n)
      case s: Show => listEpisodesForAShow(s)
    }

  def find(c: Content): ServiceOperation[Content] = c match {
    case Network(id, _, _, _, _, _) =>
      findNetwork(id).asInstanceOf[ServiceOperation[Content]]
    case Show(id, _, _, _, _, _, _, _, _) =>
      findShow(id).asInstanceOf[ServiceOperation[Content]]
    case Episode(id, _, _, _, _, _, _, _, _, _) =>
      findEpisode(id).asInstanceOf[ServiceOperation[Content]]
  }

  def find(id: ID, contentType: ContentType): ServiceOperation[Content] =
    contentType match {
      case NetworkType =>
        findNetwork(id).asInstanceOf[ServiceOperation[Content]]
      case ShowType => findShow(id).asInstanceOf[ServiceOperation[Content]]
      case EpisodeType =>
        findEpisode(id).asInstanceOf[ServiceOperation[Content]]
    }
}
