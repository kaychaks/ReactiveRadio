package radio.v0
package service
package interpreter

import java.time.LocalDate

import cats.data.ValidatedNel
import radio.v0.domain._
import radio.v0.repository.interpreter.{ContentRepoFileSystem, SyndicationRepoFileSystem}
import org.scalacheck.Prop._
import org.scalacheck.{Arbitrary, Gen, Properties}

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

object SyndicationServiceCheck extends Properties("Syndication Service"){


  implicit def arbitraryDate = Arbitrary {
    for {
      yr <- Gen.const(2016)
      m <- Gen.choose(1,12)
      dom <- Gen.choose(1,30)
    } yield LocalDate.of(yr,m,dom)
  }

  def generateNetwork = for {
      id <- Gen.alphaStr suchThat (x => x.nonEmpty && x.length == 5)
      title <- Gen.alphaStr suchThat (x => x.nonEmpty)
    } yield Network(id, title)

  def genStrings: Gen[(String,String)] = for {
    id <- Gen.uuid.map(_.toString)
    title <- Gen.alphaStr suchThat (_.nonEmpty)
  } yield (id, title)

//  property("newly created network's feed should not have any items") = forAll(genStrings) {
//    (idt: (String,String)) =>
//      val n = Network(idt._1, idt._2)
//      //      collect(n) {
//      import radio.v0.service.interpreter.FeedContentMakers._
//      object Repo extends SyndicationRepoFileSystem with ContentRepoFileSystem
//      val f = SyndicationService.createFeed(n)
//
//      val fe: Future[ValidData[Feed]] = f.run(Repo)
//
//      Await.result(fe, Duration.Inf).asInstanceOf[ValidatedNel[_, _]].isInvalid
//    //      }
//  }

}
