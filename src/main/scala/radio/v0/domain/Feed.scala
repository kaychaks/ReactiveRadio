package radio.v0
package domain

import java.net.URL
import java.nio.file.Path

sealed trait FeedType
sealed trait Rss extends FeedType
sealed trait Atom extends FeedType

sealed trait FeedContentEnconding
sealed trait FeedXML extends FeedContentEnconding

case object RSS2 extends Rss
case object ATOM extends Atom
case object XML extends FeedXML

case class Feed(content: String, fileName: Path, url: URL)

