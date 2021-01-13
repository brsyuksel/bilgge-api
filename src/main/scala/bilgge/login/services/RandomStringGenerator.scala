package bilgge.login.services

import scala.util.Random

import cats.effect._

import bilgge.login.StringGenerator

abstract class RandomStringGenerator[F[_]: Sync] extends StringGenerator[F] {
  override def generate(length: Int): F[String] =
    Sync[F].delay(Random.alphanumeric.take(length).mkString)
}
