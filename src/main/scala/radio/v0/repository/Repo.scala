package radio.v0
package repository

import java.io.File
import java.nio.file.{Files, Path, Paths}

import akka.Done
import akka.stream.scaladsl.{FileIO, Source}
import akka.util.ByteString
import radio.init.RadioDomain._
import radio.v0.domain.Content
import radio.v0.domain.customexceptions.CustomException
import radio.v0.domain.customexceptions.CustomException.{
  ContentNotFound,
  ErrorDeletingContent,
  ErrorStoringContent
}
import radio.v0.repository.interpreter.{
  ConfigurationRepoInterpretor,
  ContentTypeFetcher
}

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

trait Repo[-AnyContent, RepoOps[_, _]] {
  def store[A <: AnyContent, T](content: A)(
      implicit r: RepoOps[A, T]): FutureValueOrCustomException[A]
  def get[A <: AnyContent, T](id: ID)(
      implicit r: RepoOps[A, T]): FutureValueOrCustomException[A]
  def delete[A <: AnyContent](id: ID)(
      implicit r: RepoOps[A, _]): FutureValueOrCustomException[Boolean]
}

class RepoFileSystemInterpreter
    extends Repo[Content, RepoOps] {
  import ConfigurationRepoInterpretor._
  import cats.data._
  import cats.implicits._
  import radio.v0.repository.interpreter.ImplicitsRepoWithActor._

  private def generateFileName(
      id: ID,
      title: String,
      ct: ContentTypeFetcher[_]): CustomException Xor Path =
    Xor.fromTry {
      Try {
        Paths.get(config.getString("application.storage.directory"),
                  s"$id-${ct.getType}-${title.take(7).replaceAll("\\s", "_")}")
      }
    }.leftMap(ex => ErrorStoringContent(Some(ex.getMessage)))

  private def searchContentInFS(id: ID): Option[File] =
    Paths
      .get(config.getString("application.storage.directory"))
      .toFile
      .listFiles
      .find(_.getName.startsWith(s"$id-"))

  override def store[A <: Content, T](content: A)(
      implicit r: RepoOps[A, T]): XorT[Future, CustomException, A] = {
    type CustExOr[R] = Xor[CustomException, R]

    def en: CustExOr[T] = r.encode(content)
    def path: CustExOr[Path] =
      generateFileName(content.id, content.title, r.ct)
    val st = en |@| path map { (encoded, p) =>
      Xor.fromTry {
        Try {
          Source.single(ByteString(r.show(encoded))).runWith(FileIO.toPath(p))
        }
      }.leftMap(ex => ErrorStoringContent(Some(ex.getMessage)))
    }

    st.flatten fold (
      ex => ex.left[A].toXorT[Future],
      ft =>
        XorT[Future, CustomException, A] {
          for {
            io <- ft
          } yield
            io.status match {
              case Success(Done) => content.right[CustomException]
              case Failure(e) =>
                ErrorStoringContent(Some(e.getMessage)).left[A]
            }
      }
    )
  }

  override def get[A <: Content, T](id: ID)(
      implicit r: RepoOps[A, T]): XorT[Future, CustomException, A] = {
    val parsed: Option[CustomException Xor A] =
      for {
        file <- searchContentInFS(id)
        str <- Files
          .readAllLines(Paths.get(file.getAbsolutePath))
          .toArray(Array(""))
          .headOption
      } yield r.decode(str)

    parsed.getOrElse(ContentNotFound().left[A]).toXorT[Future]
  }

  override def delete[A <: Content](id: ID)(
      implicit r: RepoOps[A, _]): XorT[Future, CustomException, Boolean] = {
    Xor.catchNonFatal {
      for {
        file <- searchContentInFS(id)
        _ = Files.delete(Paths.get(file.getAbsolutePath))
      } yield true
    }.leftMap[CustomException](ex =>
        ErrorDeletingContent(Some(ex.getMessage)))
      .map(_.getOrElse(false))
      .toXorT[Future]
  }

}

object Repo extends RepoFileSystemInterpreter
