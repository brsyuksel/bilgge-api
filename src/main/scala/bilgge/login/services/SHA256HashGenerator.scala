package bilgge.login.services

import java.security.MessageDigest

import cats.effect._

import bilgge.login.HashGenerator

abstract class SHA256HashGenerator[F[_]: Sync] extends HashGenerator[F] {
  override def hash(data: String, salt: String): F[String] =
    Sync[F].delay {
      val plain = s"$data.$salt".getBytes
      MessageDigest
        .getInstance("SHA-256")
        .digest(plain)
        .map("%02x".format(_))
        .mkString
    }
}
