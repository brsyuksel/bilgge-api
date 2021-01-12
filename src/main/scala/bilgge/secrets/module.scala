package bilgge.secrets

import java.util.UUID

import cats._
import cats.implicits._
import cats.data._

import bilgge.users._
import bilgge.collections._
import bilgge.common._

final case class SecretListing(total: Int, data: List[Secret])

abstract class SecretsModule[F[_]](
  userRepo: UserRepository[F],
  collectionRepo: CollectionRepository[F],
  secretRepo: SecretRepository[F]
)(implicit F: MonadError[F, Throwable]) {
  def create(userId: UUID,
             collId: UUID,
             `type`: String,
             title: String,
             content: String,
             iv: String,
             hashes: List[String]): F[Secret] =
    for {
      _ <- F.fromEither(
        validation.validateExc(`type`, title, content, iv, hashes)
      )
      u <- userRepo.getBy(userId)
      _ <- F.fromOption(u, BilggeException.notFound("user not found"))
      c <- collectionRepo.getBy(collId, userId)
      _ <- F.fromOption(c, BilggeException.notFound("collection not found"))
      secret = Secret(
        userId = userId,
        collectionId = collId,
        `type` = `type`,
        title = title,
        content = content,
        iv = iv,
        hashes = hashes
      )
      created <- secretRepo.create(secret)
    } yield created

  def update(userId: UUID,
             id: UUID,
             collId: UUID,
             `type`: String,
             title: String,
             content: String,
             iv: String,
             hashes: List[String]): F[Secret] =
    for {
      _ <- F.fromEither(
        validation.validateExc(`type`, title, content, iv, hashes)
      )
      u <- userRepo.getBy(userId)
      _ <- F.fromOption(u, BilggeException.notFound("user not found"))
      c <- collectionRepo.getBy(collId, userId)
      _ <- F.fromOption(c, BilggeException.notFound("collection not found"))
      s <- secretRepo.getBy(id, userId)
      orig <- F.fromOption(s, BilggeException.notFound("secret not found"))
      secret = orig.copy(
        `type` = `type`,
        title = title,
        content = content,
        iv = iv,
        hashes = hashes
      )
      updated <- secretRepo.update(secret)
    } yield updated

  def delete(userId: UUID, id: UUID): F[Unit] =
    for {
      u <- userRepo.getBy(userId)
      _ <- F.fromOption(u, BilggeException.notFound("user not found"))
      s <- secretRepo.getBy(id, userId)
      secret <- F.fromOption(s, BilggeException.notFound("secret not found"))
      _ <- secretRepo.delete(secret)
    } yield ()

  def get(userId: UUID, id: UUID): F[Secret] =
    for {
      u <- userRepo.getBy(userId)
      _ <- F.fromOption(u, BilggeException.notFound("user not found"))
      s <- secretRepo.getBy(id, userId)
      secret <- F.fromOption(s, BilggeException.notFound("secret not found"))
    } yield secret

  def list(userId: UUID,
           collId: UUID,
           hashes: List[String],
           offset: Int,
           limit: Int): F[SecretListing] =
    for {
      u <- userRepo.getBy(userId)
      _ <- F.fromOption(u, BilggeException.notFound("user not found"))
      c <- collectionRepo.getBy(collId, userId)
      _ <- F.fromOption(c, BilggeException.notFound("collection not found"))
      total <- secretRepo.totalBy(userId, collId, hashes)
      secrets <- secretRepo.listBy(userId, collId, hashes, offset, limit)
    } yield SecretListing(total, secrets)
}

private[secrets] object validation {
  type ValidRes[A] = ValidatedNel[String, A]
  private[this] def nonEmptyStr(s: String, m: String): ValidRes[String] =
    if (s.nonEmpty) s.validNel
    else m.invalidNel

  def `type`(s: String): ValidRes[String] =
    nonEmptyStr(s, "type can not be empty")

  def title(s: String): ValidRes[String] =
    nonEmptyStr(s, "title can not be empty")

  def content(s: String): ValidRes[String] =
    nonEmptyStr(s, "content can not be empty")

  def iv(s: String): ValidRes[String] =
    nonEmptyStr(s, "_iv can not be empty")

  def hashes(h: List[String]): ValidRes[List[String]] =
    h match {
      case Nil => "hashes can not be empty".invalidNel
      case l if l.map(_.isEmpty).foldLeft(false)((c, t) => c || t) =>
        "hashes can not contain an empty hash".invalidNel
      case _ => h.validNel
    }

  def validate(tp: String, t: String, c: String, i: String, h: List[String]) =
    (`type`(tp), title(t), content(c), iv(i), hashes(h))
      .mapN((_, _, _, _, _) => ().validNel)

  def validateExc(tp: String,
                  t: String,
                  c: String,
                  i: String,
                  h: List[String]) =
    validate(tp, t, c, i, h).toEither
      .leftMap(m => BilggeException.validation(m.toList))
}
