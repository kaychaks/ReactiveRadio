package radio

package object v1{

  import cats.data._
  import scala.concurrent.Future

  type ID = String
  type Repo
  type ServiceOpn[F[_],R,T] = Kleisli[F,R,Either[String,T]]
  type FutureOpn[R,T] = ServiceOpn[Future, R, T]
  type FutureOpnWithRepo[T] = FutureOpn[Repo,T]
  type SO[T] = FutureOpnWithRepo[T]
}
