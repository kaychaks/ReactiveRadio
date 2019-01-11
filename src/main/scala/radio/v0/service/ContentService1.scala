package radio.v0
package service

import radio.v0.ID
import java.time.LocalDate

// TODO
case class ContentCreationParams()

trait ContentOps[C] {
  def createContent(p: ContentCreationParams): C
  def findContent(id: ID): C
  def listOf[B](a: C): Vector[B]
  def addContentOf[B](a: C)(b: B): C
}

trait FeedService[C,F] {
  def show(content: C) : F
}

trait BaseService[F[_],G[_,_,_],R] {
  type SO[T] = G[F[_],R,T]
}

sealed trait NetworkService1[F[_], Network, Repo, ServiceOpn[_,_,_]] extends BaseService[F, ServiceOpn,Repo]{
  def createNetwork(title: String, desc: String, cd: LocalDate): SO[Network]
  def listShows[Show](id: String): SO[List[Show]]
  def findNetwork(id: String): SO[Network]
  def showFeed[Feed](id: String)(implicit fs: FeedService[Network,Feed]): SO[Feed]
}


trait ContentService1[F[_], N, S, E, R, SO[_,_,_]] extends NetworkService1[F, N, R, SO]

