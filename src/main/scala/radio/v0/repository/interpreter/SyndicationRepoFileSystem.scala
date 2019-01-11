package radio.v0
package repository
package interpreter

import java.nio.file.{Path, Paths}

import akka.stream.IOResult
import akka.stream.scaladsl.{FileIO, Source}
import akka.util.ByteString
import cats.data.{Xor, XorT}
import radio.v0.domain.Feed
import radio.v0.domain.customexceptions.CustomException
import radio.v0.domain.customexceptions.CustomException.SyndicationError

import scala.concurrent.Future
import scala.util.{Failure, Success}

import cats.implicits._
import ImplicitsRepoWithActor._
trait SyndicationRepoFileSystem extends SyndicationRepo{
  override def store(f: Feed): XorT[Future, CustomException, Feed] = {
    val fs: Future[IOResult] = Source.single(ByteString(f.content))
      .runWith(FileIO.toPath(f.fileName))

    XorT {
      fs.flatMap(io => io.status match {
        case Success(_) => Future {f.right}
        case Failure(e) => Future {SyndicationError(Some(e.getMessage)).left}
      })
    }
  }
}

object SyndicationRepoFileSystem extends SyndicationRepoFileSystem
