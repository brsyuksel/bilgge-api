package bilgge.login

import cats._
import cats.implicits._

import bilgge.users._
import bilgge.common._

final case class Authorized(token: String,
                            publicKey: String,
                            key: String,
                            salt: String)

abstract class LoginModule[F[_]: Monad](
  hashSecret: String,
  userRepo: UserRepository[F],
  generator: StringGenerator[F],
  encrypt: Encrypt[F],
  checksum: HashGenerator[F],
  token: Token[F]
)(implicit F: MonadError[F, Throwable]) {

  def request(username: String): F[String] =
    for {
      u <- userRepo.getBy(username)
      user <- F.fromOption(u, BilggeException.notFound("user not found"))
      plain <- generator.generate(32)
      cipher <- encrypt.encrypt(user.publicKey, plain)
      hash <- checksum.hash(plain, hashSecret)
      updateUser = user.copy(loginToken = hash.some)
      _ <- userRepo.update(updateUser)
    } yield cipher

  def authorize(username: String, plain: String): F[Authorized] =
    for {
      u <- userRepo.getBy(username)
      user <- F.fromOption(u, BilggeException.notFound("user not found"))
      userId <- F.fromOption(
        user.id,
        BilggeException.internal("something went wrong")
      )
      loginToken <- F.fromOption(
        user.loginToken,
        BilggeException.notFound("perform login request first")
      )
      hash <- checksum.hash(plain, hashSecret)
      _ <- F.ifM((loginToken === hash).pure[F])(
        F.unit,
        F.raiseError(BilggeException.validation("plain does not match"))
      )
      payload = Map[String, Any](
        "username" -> user.username,
        "user_id" -> userId.toString
      )
      jwt <- token.sign(payload)
      auth = Authorized(jwt, user.publicKey, user.key, user.salt)
    } yield auth
}
