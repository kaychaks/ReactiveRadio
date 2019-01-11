package radio.v0
package service
package interpreter

import java.net.URL
import java.nio.file.Paths
import java.time.LocalDateTime

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success, Try}

import radio.v0.domain._
import radio.v0.domain.customexceptions.CustomException.SyndicationError

object FeedContentMakers {

  import cats.data._
  import cats.implicits._

  private case class ItemContent(
      title: String,
      url: URL,
      pubDate: LocalDateTime,
      authors: Vector[String],
      description: Option[String] = None,
      tags: Vector[String],
      imageURL: Option[URL] = None,
      mediaURL: URL,
      mediaSize: Long
  )

  private case class FeedContent(
      feedType: FeedType,
      encoding: FeedContentEnconding,
      feedTitle: String,
      feedFileName: String,
      feedURL: URL,
      feedDescription: Option[String] = None,
      feedImageURL: Option[URL] = None,
      feedPubDate: Option[LocalDateTime] = Some(LocalDateTime.now()),
      feedLastBuildDate: Option[LocalDateTime] = Some(LocalDateTime.now()),
      feedItems: Vector[ItemContent] = Vector.empty
  )

  private def mainScaffolding(f: FeedContent): scala.xml.Elem = {
    // TODO: categories, owners & keywords
    <rss xmlns:itunes="http://www.itunes.com/dtds/podcast-1.0.dtd" version="2.0">
      <channel>
        <title>
          {f.feedTitle}
        </title>
        <description>
          {f.feedDescription}
        </description>
        <link>
          {f.feedURL}
        </link>
        <language>en</language>
        <generator>Reactive Radio</generator>
        <pubDate>
          {f.feedPubDate.getOrElse(LocalDateTime.now()).toString}
        </pubDate>
        <lastBuildDate>
          {f.feedLastBuildDate.getOrElse(LocalDateTime.now()).toString}
        </lastBuildDate>
        <image>
          <url>
            {f.feedImageURL.toString}
          </url>
          <title>
            {f.feedTitle}
          </title>
          <link>
            {f.feedURL}
          </link>
          <description>
            {f.feedDescription}
          </description>
          <width>144</width>
          <height>144</height>
        </image>
        <itunes:summary>
          {f.feedDescription}
        </itunes:summary>
        <itunes:image href="{imageURL.toString}"/>{items(f.feedItems)}

      </channel>
    </rss>
  }

  private def items(cs: Vector[ItemContent]): Vector[scala.xml.Elem] = {
    // TODO: comments, pagination
    cs.map(c => <item>
        <title>
          {c.title}
        </title>
        <guid>
          {c.url.toString}
        </guid>
        <pubDate>
          {c.pubDate.toString}
        </pubDate>
        <author>
          {c.authors.fold("")((a, b) => s"$a & $b")}
        </author>
        <itunes:author>
          {c.authors.fold("")((a, b) => s"$a & $b")}
        </itunes:author>
        <description>
          {c.description}
        </description>
        <itunes:summary>
          {c.description}
        </itunes:summary>
        <itunes:subtitle>
          {c.description.take(100) + "..."}
        </itunes:subtitle>
        <itunes:keywords>
          {c.tags.fold("")((a, b) => s"$a , $b")}
        </itunes:keywords>
        <media:thumb url="{c.imageURL}" width="160" height="90"/>
        <enclosure url="{c.mediaURL}" length="78598398" type="audio/mpeg"/>
        <media:content lang="en" medium="audio" url={c.mediaURL.toString} expression="full" fileSize={c.mediaSize.toString} type="audio/mpeg" isDefault="true"/>
        <image>
          <url>
            {c.imageURL}
          </url>
          <title>
            {c.title}
          </title>
          <width>128</width>
          <height>72</height>
        </image>
        <itunes:image href={c.imageURL.toString}/>
      </item>)
  }

  trait FeedMakingConfig {
    def urlBase: String

    def fileBase: String

    def feedImageURL: URL
  }

  case object FeedMakingConfig extends FeedMakingConfig {
    def urlBase: String = config.getString("urlBase")

    def fileBase: String = config.getString("fileBase")

    def feedImageURL: URL = config.getAnyRef("feedImageURL").asInstanceOf[URL]
  }

  private def replaceSpacesByDashes = (_: String).replaceAll("\\s", "-")

  private def genURL(filename: String, base: String): Option[URL] =
    Some(
      new URL(
        s"/${replaceSpacesByDashes(base)}/${replaceSpacesByDashes(filename)}"))

  private def genFileName(id: ID,
                          title: String,
                          base: String,
                          ext: String): Option[String] =
    Some(s"$base/${id}_${replaceSpacesByDashes(title)}.$ext")

  private def genFeedItems(
      episodes: Vector[Episode],
      show: Show,
      config: FeedMakingConfig,
      count: Int = 10): Xor[SyndicationError, Vector[ItemContent]] = {

    Try[Vector[ItemContent]] {
      for {
        episode <- episodes.take(count)
        url <- genURL(episode.title, s"${config.urlBase}/${show.title}")
        pubDate <- episode.recordingDate
        media <- episode.media
        mediaUrl <- genURL(media.filePath, config.fileBase)
      } yield
        ItemContent(episode.title,
                    url,
                    pubDate,
                    show.hosts.map(_.name),
                    episode.description,
                    episode.hashtags.map(_.tagName),
                    episode.albumArt,
                    mediaUrl,
                    media.size)
    } match {
      case Success(f) => Xor.right(f)
      case Failure(e) => Xor.left(SyndicationError(Some(e.getMessage)))
    }
  }

  implicit val networkFeeds =
    new FeedContentMaker[Network, Episode, Feed, Rss, FeedXML, RepoContent] {

      override def generateFeed[E <: Episode](network: Network, episodes:
      Vector[E])(
          implicit r: RepoContent): Xor[SyndicationError, Feed] = {

        type XorS[T] = Xor[SyndicationError, T]

        def genFileNameAndURL: XorS[Option[(String, URL)]] = {
          Try[Option[(String, URL)]] {
            for {
              fileName <- genFileName(network.id,
                                      network.title,
                                      FeedMakingConfig.fileBase,
                                      "xml")
              url <- genURL(fileName, FeedMakingConfig.urlBase)
            } yield (fileName, url)
          } match {
            case Success(o) => Xor.right(o)
            case Failure(e) => Xor.left(SyndicationError(Some(e.getMessage)))
          }
        }

        def genItems: XorS[Option[Vector[ItemContent]]] = {
          val s = XorT[Future, SyndicationError, Option[Vector[ItemContent]]] {
            for {
              epShowTuples <- episodes
                .traverse[Future, (Episode, ValidData[Show])](e =>
                  ContentService.findShow(e.showId.get).run(r).map((e, _)))
              (ep, validShow) <- epShowTuples
              feed <- genFeedItems(episodes,
                                   validShow.toOption.get,
                                   config = FeedMakingConfig)
            } yield feed
          }

          Await.result(s.value, Duration.Inf)
        }

        val f = (genFileNameAndURL |@| genItems) map {
          case (Some((fileName, url)), Some(items)) =>
            val feedContent = FeedContent(RSS2,
                                          XML,
                                          network.title,
                                          fileName,
                                          url,
                                          network.description,
                                          Some(FeedMakingConfig.feedImageURL),
                                          Some(LocalDateTime.now()),
                                          Some(LocalDateTime.now()),
                                          items)

            Feed(mainScaffolding(feedContent).toString,
                 Paths.get(feedContent.feedFileName),
                 feedContent.feedURL)

          case _ => println("exception"); throw new Exception
        }
        f
      }
    }

}
