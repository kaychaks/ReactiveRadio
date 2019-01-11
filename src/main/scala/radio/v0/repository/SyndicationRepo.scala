package radio.v0
package repository

import java.nio.file.Path

import cats.data.XorT
import radio.v0.domain.Feed
import radio.v0.domain.customexceptions.CustomException

import scala.concurrent.Future

trait SyndicationRepo{
  def store(f: Feed) : XorT[Future,CustomException,Feed]
  def fetch(f: Path) : XorT[Future,CustomException,Feed]
}
