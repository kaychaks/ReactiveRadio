import cats.data.{Kleisli, Validated, Xor, XorT}
import radio.v0.service.interpreter.{EpisodeServiceInterpreter, NetworkServiceInterpreter, ShowServiceInterpreter}

import scala.concurrent.Future

trait FeedType
trait EncType
trait RSS2 extends FeedType
trait XML extends EncType

trait Content
trait FeedableContent extends Content
trait PlayableContent extends Content
case class Network(id: String, title: String) extends FeedableContent
case class Show(id: String,
                title: String,
                hosts: Vector[String],
                networkId: String)
    extends FeedableContent
case class Episode(id: String, title: String, notes: String, showId: String)
    extends PlayableContent

case class Feed(content: String, path: String)

trait RepoOps[A, T] {
  def encode(a: A): String Xor T
  def decode(t: T): String Xor A
  def show(t: T): String
}
trait Repo[-AnyContent, RepoOps[_, _]] {
  def store[A <: AnyContent, T](content: A)(
      implicit r: RepoOps[A, T]): XorT[Future, String, A]
  def get[A <: AnyContent, T](id: String)(
      implicit r: RepoOps[A, T]): XorT[Future, String, A]
  def delete[A <: AnyContent](id: String)(
      implicit r: RepoOps[A, _]): XorT[Future, String, A]
}

trait ContentService
trait NetworkService extends ContentService {
  def createNetwork(
      title: String): Kleisli[Future, Repo, Validated[String, Network]]
  def fetchNetwork(
      id: String): Kleisli[Future, Repo, Validated[String, Network]]
  def findEpisodes(n: Network): Kleisli[Future, Repo, Validated[String, Vector[Episode]]]
  // other network related services
}
trait ShowService extends ContentService {
  def createShow(title: String): Kleisli[Future, Repo, Validated[String, Show]]
  def fetchShow(id: String): Kleisli[Future, Repo, Validated[String, Show]]
  def findEpisodes(n: Show): Kleisli[Future, Repo, Validated[String, Vector[Episode]]]
  // other show related services
}
trait EpisodeService extends ContentService {
  def createEpisode(
      title: String): Kleisli[Future, Repo, Validated[String, Show]]
  def fetchEpisode(id: String): Kleisli[Future, Repo, Validated[String, Show]]
  // other episode related services
}

object ContentService extends NetworkServiceInterpreter with ShowServiceInterpreter with EpisodeServiceInterpreter

trait FeedMaker[Content, ItemContent, Feed, FeedType, Encoding] {
  def genFeed(c: Content, is: Vector[ItemContent]): Xor[String, Feed]
}

trait FeedService[Content, ItemContent, Feed] {
  def createFeed[FC <: Content, IC <: ItemContent, FeedType, Encoding](
      content: FC)(implicit sh: FeedMaker[FC, IC, Feed, FeedType, Encoding])
    : Kleisli[Future, Repo, Validated[String, Feed]]
}

class FeedServiceInterpreter
    extends FeedService[FeedableContent, PlayableContent, Feed] {

  implicit val networkFeedMaker = new FeedMaker[Network, Episode, Feed, RSS2, XML] {
    override def genFeed(c: Network, is: Vector[Episode]): Xor[String, Feed] = {
      // for some operation
      for {
        episodeShowTupleVec <- is.traverse[Future, Validated[String, Show]](e => ContentService
          .fetchShow(e
          .showId).run(repo).map(
          (e,_)))
        (ep, show) <- episodeShowTupleVec
        feed <- privateGenFeedFun(c, )
      }

    }
  }

  override def createFeed[FC <: FeedableContent,
                          IC <: PlayableContent,
                          FeedType,
                          Encoding](content: FC)(
      implicit sh: FeedMaker[FC, IC, Feed, FeedType, Encoding])
    : Kleisli[Future, Repo, Validated[String, Feed]] =
    for {
      episodes <- ContentService.findEpisodes(content)
      feed <- Kleisli[Future,Repo,Validated[String, Feed]] { repo =>
        Future(
          sh.genFeed(content, episodes.toOption.get).toValidated)
      }
    } yield feed
}
