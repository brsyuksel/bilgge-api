package bilgge.common

sealed trait Reason
object Reason {
  case object NotFound extends Reason {
    override def toString: String = "not_found"
  }

  case object Validation extends Reason {
    override def toString: String = "validation"
  }

  case object Internal extends Reason {
    override def toString: String = "internal_error"
  }
}

final case class BilggeException(reason: Reason, messages: List[String])
    extends Exception(reason.toString)

object BilggeException {
  def notFound(messages: List[String]) =
    new BilggeException(Reason.NotFound, messages)

  def notFound(message: String): BilggeException =
    notFound(List(message))

  def validation(messages: List[String]) =
    new BilggeException(Reason.Validation, messages)

  def validation(message: String): BilggeException =
    validation(List(message))

  def internal(messages: List[String]) =
    new BilggeException(Reason.Internal, messages)

  def internal(message: String): BilggeException =
    internal(List(message))
}
