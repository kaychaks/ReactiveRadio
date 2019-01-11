package radio.v0.repository.interpreter

import java.time.LocalDate

import radio.v0.domain.Network
import radio.v0.domain.customexceptions.CustomException
import radio.v0.repository.Repo$$
import org.scalacheck.{Arbitrary, Gen, Properties}
import org.scalacheck.Prop._
import spray.json.JsValue

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration

object ContentRepoFileSystemCheck extends Properties("Content Repo File System Interpreter") {

  import cats.implicits._
  import cats.data._
  import ImplicitsRepoWithActor._

  implicit def arbitraryDate = Arbitrary {
    for {
      yr <- Gen.const(2016)
      m <- Gen.choose(1, 12)
      dom <- Gen.choose(1, 20)
    } yield LocalDate.of(yr, m, dom)
  }

  val idGen: Gen[String] = Gen.uuid.map(_.toString)
  val titleGen: Gen[String] = Gen.alphaStr suchThat (_.length > 0)
  val descGen: Gen[String] = Gen.alphaStr
  val dtGen: Gen[LocalDate] = arbitraryDate.arbitrary

  val newNetworkGen: Gen[Network] = for {
    id <- idGen
    title <- titleGen
    desc <- descGen
    descOpt <- Gen.oneOf(None, Some(desc))
    dt <- dtGen
    dtOpt <- Gen.oneOf(None, Some(dt))
  } yield Network(
    id = id,
    title = title,
    description = descOpt,
    creationDate = dtOpt,
    rssFeeds = Vector.empty,
    showIds = Vector.empty
  )

  property("what goes in must come out - a new network") = forAll(newNetworkGen) {
    network: Network =>
      import ContentRepoOps._

      def save = Repo$.store[Network, JsValue](network)

      def fetch = Repo$.get[Network, JsValue](network.id)

      val cmp: XorT[Future, CustomException, Boolean] = for {
        s <- save
        g <- fetch
      } yield s.id == g.id

      val ret = Await.result(cmp.value, Duration.Inf)
      Repo$.delete[Network](network.id)
      ret.toOption.getOrElse(false)
  }
}
