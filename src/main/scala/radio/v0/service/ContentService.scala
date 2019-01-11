package radio.v0
package service

import java.net.URL
import java.time.{Duration, LocalDate, LocalDateTime}

sealed trait ContentType

case object NetworkType extends ContentType
case object ShowType extends ContentType
case object EpisodeType extends ContentType

sealed trait ContentService

trait NetworkService[Network, Show] extends ContentService {

  def createNetwork(title: String,
                    description: Option[String] = None,
                    creationDate: Option[LocalDate] = Some(LocalDate.now()))
    : ServiceOperation[Network]

  def listShows(n: Network): ServiceOperation[Vector[Show]]

  def findNetwork(id: ID): ServiceOperation[Network]
}

trait ShowService[Show, Network, Episode] extends ContentService {

  def createShow(title: String,
                 creationDate: Option[LocalDate] = Some(LocalDate.now()),
                 hostNameEmails: Vector[(String, String)] = Vector.empty,
                 description: Option[String] = None,
                 albumArt: Option[URL] = None,
                 networkId: Option[ID] = None): ServiceOperation[Show]

  def addShowToNetwork(s: Show, n: Network): ServiceOperation[Show]

  def listEpisodesForAShow(s: Show): ServiceOperation[Vector[Episode]]

  def listEpisodesForShows(s: Vector[Show]): ServiceOperation[Vector[Episode]]

  def findShow(id: ID): ServiceOperation[Show]
}

trait EpisodeService[Episode, Show, Media, Tag] extends ContentService {

  def createEpisode(title: String,
                    creationDate: Option[LocalDate] = Some(LocalDate.now()),
                    description: Option[String] = None,
                    albumArt: Option[URL] = None,
                    duration: Option[Duration] = None,
                    showNotes: Option[String] = None,
                    recordingDate: Option[LocalDateTime] = None,
                    hashtags: Seq[String] = Seq.empty,
                    mediaFilePath: Option[String] = None,
                    showId: Option[ID] = None,
                    show: Option[Show] = None): ServiceOperation[Episode]

  def addEpisodeToShow(e: Episode, s: Show): ServiceOperation[Episode]

  def addMediaToEpisode(mediaFileURL: String,
                        e: Episode): ServiceOperation[Episode]

  def addTagsToEpisode(t: Seq[Tag], e: Episode): ServiceOperation[Episode]

  def removeTagsFromEpisode(t: Seq[Tag], e: Episode): ServiceOperation[Episode]

  def updateShowNotes(notes: String, e: Episode): ServiceOperation[Episode]

  def updateMedia(media: Media, e: Episode): ServiceOperation[Episode]

  def findEpisode(id: ID): ServiceOperation[Episode]

}
