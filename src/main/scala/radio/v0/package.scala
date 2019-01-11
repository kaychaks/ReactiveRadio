package radio

package object v0 {

  import cats.data.{Kleisli, ValidatedNel}
  import domain.customexceptions.CustomException
  import repository.AppConfig
  import scala.concurrent.Future

  type ValidData[+A] = ValidatedNel[CustomException, A]

  type ConfigType = AppConfig[_, _, _]

  type ServiceOperation[F[_],A] = Kleisli[F, ConfigType, ValidData[A]]

  type ID = String
}
