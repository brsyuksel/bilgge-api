package bilgge.login

import java.util.UUID

trait StringGenerator[F[_]] {
  def generate(length: Int): F[String]
}

trait Encrypt[F[_]] {
  def encrypt(publicKey: String, plain: String): F[String]
}

trait HashGenerator[F[_]] {
  def hash(data: String): F[String]
}

final case class Claim(userId: UUID, username: String)
trait Token[F[_]] {
  def sign(c: Claim): F[String]
  def verify(token: String): F[Claim]
}
