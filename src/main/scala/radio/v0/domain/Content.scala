package radio.v0
package domain

import java.net.URL
import java.time.{LocalDate, LocalDateTime}

sealed trait Content
sealed trait FeedableContent extends Content
sealed trait TaggableContent extends Content
sealed trait ActionableContent extends Content

final case class Network(
  id: ID,
  title: String,
  description: Option[String] = None,
  creationDate: Option[LocalDate] = Some(LocalDate.now()),
  rssFeeds: Vector[Feed] = Vector.empty[Feed]
) extends FeedableContent

final case class Show(
  id: ID,
  title: String,
  creationDate: Option[LocalDate] = Some(LocalDate.now()),
  hosts: Vector[Host] = Vector.empty,
  episodeIds: Vector[ID] = Vector.empty,
  description: Option[String] = None,
  albumArt: Option[URL] = None,
  rssFeeds: Vector[Feed] = Vector.empty,
  networkId: Option[ID] = None
) extends FeedableContent

final case class Episode(
  id: ID,
  title: String,
  description: Option[String] = None,
  creationDate: Option[LocalDate] = Some(LocalDate.now()),
  recordingDate: Option[LocalDateTime] = Some(LocalDateTime.now()),
  showNotes: Option[String] = None,
  media: Option[Media] = None,
  albumArt: Option[URL] = None,
  hashtags: Vector[Tag] = Vector.empty[Tag],
  showId: Option[ID] = None,
  rssFeeds: Vector[Feed],
  tags: Vector[Tag]
) extends ActionableContent with TaggableContent

