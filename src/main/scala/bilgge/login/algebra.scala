package bilgge.login

trait StringGenerator[F[_]] {
  def generate(length: Int): F[String]
}

trait Encrypt[F[_]] {
  def encrypt(publicKey: String, plain: String): F[String]
}

trait HashGenerator[F[_]] {
  def hash(data: String, salt: String): F[String]
}

trait Token[F[_]] {
  def sign(payload: Map[String, Any]): F[String]
}
