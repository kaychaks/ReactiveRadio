package radio.v0.service.interpreter

import java.net.URL
import java.time.LocalDate
import java.util.UUID

import cats.data.Validated.{Invalid, Valid}
import radio.v0.ValidData
import radio.v0.domain.{Episode, Network, Show}
import radio.v0.repository.Repo$$
import org.scalacheck.Prop._
import org.scalacheck.{Arbitrary, Gen, Properties}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

object ContentServiceSpec extends Properties("Content Service Interpreter") {

  import ContentService._
  import cats.implicits._
  import radio.v0.repository.interpreter.ImplicitsRepoWithActor._

  implicit def arbitraryDate = Arbitrary {
    for {
      yr <- Gen.const(2016)
      m <- Gen.choose(1, 12)
      dom <- Gen.choose(1, 20)
    } yield LocalDate.of(yr, m, dom)
  }

  val strGen: Gen[String] = Gen.alphaStr suchThat (!_.isEmpty)

  val urlGen = for {
    proto <- Gen.oneOf[String]("http", "https")
    path <- Gen.alphaStr
  } yield new URL(proto + "://" + path)

  val emailGen = for {
    preAt <- strGen
    postAt <- Gen.alphaStr suchThat (x => x.length > 0 && x.length <= 60)
    domain <- Gen.alphaStr suchThat (x => x.length > 0 && x.length <= 60)
  } yield preAt + "@" + postAt + "." + domain

  val hostEmailListGen: Gen[List[(String, String)]] = Gen.listOf[(String,
    String)] {
    for {
      name <- strGen
      email <- emailGen
    } yield (name, email)
  }

  val genCreateNetworkParams: Gen[(String, Option[String], Option[LocalDate])] = for {
    title <- strGen
    desc <- strGen
    descOpt <- Gen.oneOf(None, Option(desc))
    dt <- arbitraryDate.arbitrary
    dtOpt <- Gen.oneOf(None, Option(dt))
  } yield (title, descOpt, dtOpt)

  val genCreateShowParam = for {
    (title, descOpt, dtOpt) <- genCreateNetworkParams
    //nameEmails <- hostEmailListGen suchThat (x => x.nonEmpty && x.length <= 2)
    nameEmails <- Gen.listOfN[(String, String)](2, Gen.oneOf(
      ("kaushik", "kaushik.chakraborty3@email.com"),
      ("Vishi", "vishi@email.com"),
      ("Biju", "biju@gmail.com")
    ))
    albumArtURL <- urlGen
    albumArtOpt <- Gen.oneOf(None, Option(albumArtURL))
    networkId <- Gen.uuid
  } yield (title, descOpt, dtOpt, albumArtOpt, networkId, nameEmails)

  val genCreateShowsParams = for {
    v <- Gen.listOf(genCreateShowParam)
  } yield v

  val p1 = forAllNoShrink(genCreateNetworkParams) {
    case (title: String, desc: Option[String], dt: Option[LocalDate]) =>
      val n = createNetwork(title, desc, dt).run(Repo$)
        .flatMap {
          case Valid(n: Network) => findNetwork(n.id).run(Repo$)
          case Invalid(ex) => Future(ex.invalidNel)
        }

      val ret = Await.result(n, Duration.Inf)

      ret.isValid :| "reflexivity in creation - Network"
  }

  val p2 = forAll(genCreateShowParam) {
    case (
      title: String, desc: Option[String],
      dt: Option[LocalDate],
      albumArt: Option[URL],
      networkId: UUID,
      nameEmails: List[(String, String)]
      ) =>

      val s = createShow(title, dt, nameEmails.toVector, desc, albumArt,
        Option(networkId.toString)).run(Repo$)
        .flatMap {
          case Valid(s: Show) => findShow(s.id).run(Repo$)
          case Invalid(ex) => Future(ex.invalidNel)
        }


      val ret = Await.result(s, Duration.Inf)

      ret.isValid :| "reflexivity in creation - Show"
  }

  val p3 = forAllNoShrink(genCreateNetworkParams) {
    case (title: String, desc: Option[String], dt: Option[LocalDate]) => {

      val sh = for {
        n <- createNetwork(title, desc, dt).run(Repo$)
        f <- n match {
          case Valid(n: Network) => listShows(n).run(Repo$)
          case Invalid(ex) => Future(ex.invalidNel)
        }
      } yield f

      val ret = Await.result(sh, Duration.Inf)

      ret.ensure(new IllegalArgumentException)(v => v.isEmpty).isValid :|
        "new network has no shows"
    }
  }

  val p4 = forAllNoShrink(genCreateShowsParams) {
    v =>
      lazy val showsF: List[Future[ValidData[Show]]] = v map {
        case (
          title: String, desc: Option[String],
          dt: Option[LocalDate],
          albumArt: Option[URL],
          networkId: UUID,
          nameEmails: List[(String, String)]
          ) =>
          createShow(title, dt, nameEmails.toVector, desc, albumArt, Option(networkId.toString)).run(Repo$)
      }

      val ef: Future[ValidData[Vector[Episode]]] = for {
        listValidShows <- showsF.sequence
        validListshows <- Future(listValidShows.sequence)
        shows <- Future(validListshows.toXor)
        episodes <- listEpisodesForShows(shows.toOption.getOrElse(Vector
          .empty).toVector).run(Repo$)
      } yield episodes

      val ret: ValidData[Vector[Episode]] = Await.result(ef,Duration.Inf)

      ret.fold(_ => false, v => v.isEmpty) :| "All new shows start with empty" +
        " episodes"
  }


  property("combined") = p4
}
