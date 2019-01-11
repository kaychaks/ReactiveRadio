package radio.v0
package domain

trait ChildrenFetcher[A,B] {
  def fetch(id : A) : Vector[B]
}

object ChildrenFetcher {
  implicit object networkShows extends ChildrenFetcher[Network,Show] {
    def fetch(n: Network) : Vector[Show] = ???
  }
}
