package radio.v0
package service
package interpreter

import java.net.URL

import cats.data.Kleisli
import cats.data.Validated.Valid
import cats.implicits._
import radio.v0.domain._
import radio.v0.domain.customexceptions.CustomException
import radio.v0.domain.customexceptions.CustomException.SyndicationError
import radio.v0.repository.{ContentRepo, SyndicationRepo}

import scala.concurrent.Future

class SyndicationServiceInterpreter
    extends SyndicationService[FeedableContent, PlayableContent, Feed] {

  import radio.v0.repository.interpreter.ImplicitsRepoWithActor._
  import FeedContentMakers._

  override def createFeed[FC <: FeedableContent,
                          IC <: PlayableContent,
                          FeedType,
                          Encoding](content: FC)(
      implicit sh: FeedContentMaker[FC,
                                    IC,
                                    Feed,
                                    FeedType,
                                    Encoding,
                                    RepoContent]): SyndicationOperation[Feed] =
    for {
      episodes <- ContentService
        .listEpisodes(content)
        .local[SyndicationRepo with RepoContent](x => x)
      feed <- Kleisli[Future,
                      SyndicationRepo with RepoContent,
                      ValidData[Feed]] { repo =>
        Future[ValidData[Feed]](
          sh.generateFeed(content, episodes.toOption.get)(repo)
            .toValidatedNel)

      }
    } yield feed

  override def addFeedToContent(
      f: Feed,
      c: FeedableContent): ServiceOperation[FeedableContent] = ???

  override def fetchFeed[A <: URL](f: A): ServiceOperation[Feed] = ???
}

object SyndicationService extends SyndicationServiceInterpreter
