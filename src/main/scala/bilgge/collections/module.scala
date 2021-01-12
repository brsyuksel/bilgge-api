package bilgge.collections

import java.util.UUID

import cats._
import cats.implicits._
import cats.data._

import bilgge.users._
import bilgge.common._

abstract class CollectionsModule[F[_]](
  userRepo: UserRepository[F],
  collectionRepo: CollectionRepository[F]
)(implicit F: MonadError[F, Throwable]) {
  def create(userId: UUID, name: String, iv: String): F[Collection] =
    for {
      _ <- F.fromEither(validation.validateExc(name, iv))
      u <- userRepo.getBy(userId)
      _ <- F.fromOption(u, BilggeException.notFound("user not found"))
      collection = Collection(userId = userId, name = name, iv = iv)
      created <- collectionRepo.create(collection)
    } yield created

  def update(userId: UUID, id: UUID, name: String, iv: String): F[Collection] =
    for {
      _ <- F.fromEither(validation.validateExc(name, iv))
      u <- userRepo.getBy(userId)
      _ <- F.fromOption(u, BilggeException.notFound("user not found"))
      c <- collectionRepo.getBy(id, userId)
      orig <- F.fromOption(c, BilggeException.notFound("collection not found"))
      coll = orig.copy(name = name, iv = iv)
      updated <- collectionRepo.update(coll)
    } yield updated

  def delete(userId: UUID, id: UUID): F[Unit] =
    for {
      u <- userRepo.getBy(userId)
      _ <- F.fromOption(u, BilggeException.notFound("user not found"))
      c <- collectionRepo.getBy(id, userId)
      coll <- F.fromOption(c, BilggeException.notFound("collection not found"))
      _ <- collectionRepo.delete(coll)
    } yield ()

  def list(userId: UUID): F[List[Collection]] =
    for {
      u <- userRepo.getBy(userId)
      _ <- F.fromOption(u, BilggeException.notFound("user not found"))
      loC <- collectionRepo.listBy(userId)
    } yield loC
}

private[collections] object validation {
  type ValidRes[A] = ValidatedNel[String, A]
  def name(s: String): ValidRes[String] =
    if (s.nonEmpty) s.validNel
    else "name can not be empty".invalidNel

  def iv(s: String): ValidRes[String] =
    if (s.nonEmpty) s.validNel
    else "_iv can not be empty".invalidNel

  def validate(n: String, i: String) =
    (name(n), iv(i)).mapN((_, _) => ().validNel)

  def validateExc(n: String, i: String) =
    validate(n, i).toEither
      .leftMap(m => BilggeException.validation(m.toList))
}
