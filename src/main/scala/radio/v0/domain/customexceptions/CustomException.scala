package radio.v0
package domain
package customexceptions

sealed trait CustomException {
  def message: Option[String]
}

sealed trait ApplicationException[T <: ApplicationException[T]] extends CustomException {
  self: T =>
  def show(implicit s: cats.Show[T]): String = s.show(self)
}

sealed trait DBException[T <: DBException[T]] extends CustomException {
  self: T =>
  def show(ex: T)(implicit s: cats.Show[T]) = s.show(ex)
}

object CustomException {

  object ShowImplicits {

    private def formatMessage(exType: String, msg: Option[String]): String = {
      require(exType.nonEmpty)

      val opening = s"EXCEPTION :: $exType"
      msg.map(m => s"$opening :: $m").getOrElse(opening)
    }

    implicit object ErrorGeneratingID extends cats.Show[ErrorGeneratingID] {
      override def show(f: ErrorGeneratingID): String = formatMessage("Error generating ID", f.message)
    }

    implicit object WrongContentTypeShow extends cats.Show[WrongContentType] {
      override def show(f: WrongContentType): String = formatMessage("Wrong Content Type", f.message)
    }

    implicit object ErrorCreatingNetwork extends cats.Show[ErrorCreatingNetwork] {
      override def show(f: ErrorCreatingNetwork): String = formatMessage("Error while creating network", f.message)
    }

    implicit object ErrorCreatingShow extends cats.Show[ErrorCreatingShow] {
      override def show(f: ErrorCreatingShow): String = formatMessage("Error while creating show", f.message)
    }

    implicit object ErrorCreatingEpisode extends cats.Show[ErrorCreatingEpisode] {
      override def show(f: ErrorCreatingEpisode): String = formatMessage("Error while creating episode", f.message)
    }

    implicit object ErrorStoringContent extends cats.Show[ErrorStoringContent] {
      override def show(f: ErrorStoringContent): String = formatMessage("Error while storing content", f.message)
    }

    implicit object ErrorDeletingContent extends cats.Show[ErrorDeletingContent] {
      override def show(f: ErrorDeletingContent): String = formatMessage("Error while deleting content", f.message)
    }

    implicit object ContentNotFound extends cats.Show[ContentNotFound] {
      override def show(f: ContentNotFound): String = formatMessage("Content not found", f.message)
    }

    implicit object SyndicationErrorShow extends cats.Show[SyndicationError] {
      override def show(f: SyndicationError): String = formatMessage("Error while syndicating", f.message)
    }

    implicit object ContentEncodingDecodingError extends cats.Show[ContentEncodingDecodingError] {
      override def show(f: ContentEncodingDecodingError): String = formatMessage("Error encoding or decoding content", f.message)
    }
  }

  final case class ErrorGeneratingID(message: Option[String] = None) extends ApplicationException[ErrorGeneratingID]

  final case class WrongContentType(message: Option[String] = None) extends ApplicationException[WrongContentType]

  final case class ErrorCreatingNetwork(message: Option[String] = None) extends ApplicationException[ErrorCreatingNetwork]

  final case class ErrorCreatingShow(message: Option[String] = None) extends ApplicationException[ErrorCreatingShow]

  final case class ErrorCreatingEpisode(message: Option[String] = None) extends ApplicationException[ErrorCreatingEpisode]

  final case class SyndicationError(message: Option[String] = None) extends ApplicationException[SyndicationError]

  final case class ErrorStoringContent(message: Option[String] = None) extends DBException[ErrorStoringContent]

  final case class ErrorDeletingContent(message: Option[String] = None) extends DBException[ErrorDeletingContent]

  final case class ContentNotFound(message: Option[String] = None) extends DBException[ContentNotFound]

  final case class ContentEncodingDecodingError(message: Option[String] = None) extends DBException[ContentEncodingDecodingError]

}

