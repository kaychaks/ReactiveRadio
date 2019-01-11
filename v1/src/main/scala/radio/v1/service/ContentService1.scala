package radio/v1
package service

import java.time.LocalDate

// TODO
case class ContentCreationParams()

case class Feed()

trait FeedService[C] {
  def show(content: C): Feed
}

object FeedService {
  def apply[A](implicit v: FeedService[A]): FeedService[A] = v
}

trait NetworkService[Network, Repo] {
  def createNetwork(title: String, desc: String, cd: LocalDate): SO[Network]

  def listShows[Show](id: String): SO[List[Show]]

  def findNetwork(id: String): SO[Network]

  def showFeed[FS <: Network: FeedService](id: String): SO[Feed]
}

trait ContentService[F[_], N, S, E, R] extends NetworkService[N, R, F]
