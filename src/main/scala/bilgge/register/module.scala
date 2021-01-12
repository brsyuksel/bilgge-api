package bilgge.register

import cats._
import cats.implicits._
import cats.data._

import bilgge.users._
import bilgge.common._

abstract class RegisterModule[F[_]](userRepo: UserRepository[F])(
  implicit F: MonadError[F, Throwable]
) {
  def createUser(username: String,
                 publicKey: String,
                 key: String,
                 salt: String): F[User] =
    for {
      _ <- F.fromEither(validation.validateExc(username, publicKey, key, salt))
      u <- userRepo.getBy(username)
      _ <- F.whenA(u.nonEmpty)(
        F.raiseError(BilggeException.validation("user already exists"))
      )
      user = User(
        username = username,
        publicKey = publicKey,
        key = key,
        salt = salt
      )
      created <- userRepo.create(user)
    } yield created
}

private[register] object validation {
  type ValidRes[A] = ValidatedNel[String, A]
  def username(s: String): ValidRes[String] =
    if (s.matches("^[a-zA-Z]+[\\w.-]+$")) s.validNel
    else "invalid username".invalidNel

  def publicKey(s: String): ValidRes[String] =
    if (s.nonEmpty) s.validNel
    else "public_key can not be empty".invalidNel

  def key(s: String): ValidRes[String] =
    if (s.nonEmpty) s.validNel
    else "key can not be empty".invalidNel

  def salt(s: String): ValidRes[String] =
    if (s.nonEmpty) s.validNel
    else "salt can not be empty".invalidNel

  def validate(u: String, pk: String, k: String, s: String) =
    (username(u), publicKey(pk), key(k), salt(s))
      .mapN((_, _, _, _) => ().validNel)

  def validateExc(u: String, pk: String, k: String, s: String) =
    validate(u, pk, k, s).toEither
      .leftMap(m => BilggeException.validation(m.toList))
}
