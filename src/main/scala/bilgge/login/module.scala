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
      hash <- checksum.hash(plain)
      updateUser = user.copy(loginToken = hash.some)
      _ <- userRepo.update(updateUser)
    } yield cipher

  def authorize(username: String, plain: String): F[Authorized] =
    for {
      _ <- F.whenA(plain.isEmpty)(
        F.raiseError(BilggeException.validation("plain can not be empty"))
      )
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
      hash <- checksum.hash(plain)
      _ <- F.ifM((loginToken === hash).pure[F])(
        F.unit,
        F.raiseError(BilggeException.validation("plain does not match"))
      )
      claim = Claim(userId, user.username)
      jwt <- token.sign(claim)
      auth = Authorized(jwt, user.publicKey, user.key, user.salt)
      updateUser = user.copy(loginToken = none)
      _ <- userRepo.update(updateUser)
    } yield auth
}
