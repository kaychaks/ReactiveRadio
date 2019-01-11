package radio.init

import java.net.URL
import java.time.{Duration, Instant}

object RadioDomain {

  type ID = String

  case class Show(
                   showID: ID,
                   showName: String,
                   creationDate: Instant = Instant.now(),
                   hosts: Vector[Host],
                   episodes: Vector[Episode],
                   description: String,
                   albumArt: Option[URL] = None,
                   rssFeed: URL
                 )

  case class Episode(
                      episodeID: ID,
                      title: String,
                      duration: Duration,
                      recordingDate: Instant = Instant.now(),
                      showNotes: String,
                      mediaFileURL: Option[URL] = None,
                      hashtags: Vector[Tag]
                    )

  case class Host(
                   name: String,
                   email: String,
                   hostID: ID
                 )

  case class Tag(
                  tagName: String,
                  tagID: ID,
                  tagURL: URL,
                  episodes: Vector[Episode]
                )

}
