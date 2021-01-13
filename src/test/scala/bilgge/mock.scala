package bilgge

import java.util.UUID
import java.time.LocalDateTime

import cats._
import cats.implicits._

import bilgge.users._
import bilgge.collections._
import bilgge.secrets._

object mock {
  val vUserId = UUID.fromString("643ecddf-5823-4c6c-8625-8183b1d30c9a")
  val vCollId1 = UUID.fromString("38ec60a1-b255-4714-9022-0ebbe4abcaaa")
  val vCollId2 = UUID.fromString("4d46d522-94b1-4eb1-8c74-5373837fa1b1")
  val vSecId1 = UUID.fromString("ffa39999-1b6e-4f9f-92eb-5bb5fca4708d")
  val vSecId2 = UUID.fromString("313de6c8-f099-431b-9381-056c2da86ad2")
  private def now = LocalDateTime.now().some

  class HCUserRepository[F[_]: Applicative] extends UserRepository[F] {
    override def create(u: User): F[User] =
      u.copy(id = UUID.randomUUID().some, createdAt = now, updatedAt = now)
        .pure[F]

    override def update(u: User): F[User] =
      u.copy(updatedAt = now).pure[F]

    override def getBy(username: String): F[Option[User]] = username match {
      case "case-1" =>
        User(
          vUserId.some,
          "case-1",
          "pubkey-1",
          "aeskey-1",
          "hashsalt-1",
          "login-token-hash-1".some,
          now,
          now
        ).some.pure[F]
      case "case-2" =>
        User(
          UUID.randomUUID().some,
          "case-2",
          "pubkey-1",
          "aeskey-1",
          "hashsalt-1",
          none,
          now,
          now
        ).some.pure[F]
      case _ => none[User].pure[F]
    }

    override def getBy(id: UUID): F[Option[User]] = id.toString match {
      case "643ecddf-5823-4c6c-8625-8183b1d30c9a" =>
        User(
          vUserId.some,
          "case-1",
          "pubkey-1",
          "aeskey-1",
          "hashsalt-1",
          "login-token-hash-1".some,
          now,
          now
        ).some.pure[F]
      case _ => none[User].pure[F]
    }
  }

  final class HCCollectionRepository[F[_]: Applicative]
      extends CollectionRepository[F] {
    override def create(c: Collection): F[Collection] =
      c.copy(id = UUID.randomUUID().some, createdAt = now, updatedAt = now)
        .pure[F]

    override def update(c: Collection): F[Collection] =
      c.copy(updatedAt = now).pure[F]

    override def delete(c: Collection): F[Unit] =
      ().pure[F]

    override def getBy(id: UUID, userId: UUID): F[Option[Collection]] =
      (id.toString, userId.toString) match {
        case (
            "38ec60a1-b255-4714-9022-0ebbe4abcaaa",
            "643ecddf-5823-4c6c-8625-8183b1d30c9a"
            ) =>
          Collection(vCollId1.some, vUserId, "coll-1", "iv-1", now, now).some
            .pure[F]
        case (
            "4d46d522-94b1-4eb1-8c74-5373837fa1b1",
            "643ecddf-5823-4c6c-8625-8183b1d30c9a"
            ) =>
          Collection(vCollId2.some, vUserId, "coll-2", "iv-2", now, now).some
            .pure[F]
        case _ =>
          none[Collection].pure[F]
      }

    override def listBy(userId: UUID): F[List[Collection]] =
      userId.toString match {
        case "643ecddf-5823-4c6c-8625-8183b1d30c9a" =>
          List(
            Collection(vCollId1.some, vUserId, "coll-1", "iv-1", now, now),
            Collection(vCollId2.some, vUserId, "coll-2", "iv-2", now, now)
          ).pure[F]
        case _ => List.empty[Collection].pure[F]
      }
  }

  final class HCSecretRepository[F[_]: Applicative]
      extends SecretRepository[F] {
    override def create(s: Secret): F[Secret] =
      s.copy(id = UUID.randomUUID().some, createdAt = now, updatedAt = now)
        .pure[F]

    override def update(s: Secret): F[Secret] =
      s.copy(updatedAt = now).pure[F]

    override def delete(s: Secret): F[Unit] =
      ().pure[F]

    override def getBy(id: UUID, userId: UUID): F[Option[Secret]] =
      (id.toString, userId.toString) match {
        case (
            "ffa39999-1b6e-4f9f-92eb-5bb5fca4708d",
            "643ecddf-5823-4c6c-8625-8183b1d30c9a"
            ) =>
          Secret(
            vSecId1.some,
            vUserId,
            vCollId1,
            "type-1",
            "title-1",
            "content-1",
            "iv-1",
            List("h1"),
            now,
            now
          ).some.pure[F]
        case (
            "313de6c8-f099-431b-9381-056c2da86ad2",
            "643ecddf-5823-4c6c-8625-8183b1d30c9a"
            ) =>
          Secret(
            vSecId2.some,
            vUserId,
            vCollId1,
            "type-1",
            "title-2",
            "content-1",
            "iv-1",
            List("h1", "h2"),
            now,
            now
          ).some.pure[F]
        case _ => none[Secret].pure[F]
      }

    override def listBy(userId: UUID,
                        collectionId: UUID,
                        hashes: List[String],
                        offset: Int,
                        limit: Int): F[List[Secret]] =
      (userId.toString, collectionId.toString, hashes) match {
        case (
            "643ecddf-5823-4c6c-8625-8183b1d30c9a",
            "38ec60a1-b255-4714-9022-0ebbe4abcaaa",
            List("h1")
            ) =>
          List(
            Secret(
              vSecId1.some,
              vUserId,
              vCollId1,
              "type-1",
              "title-1",
              "content-1",
              "iv-1",
              List("h1"),
              now,
              now
            ),
            Secret(
              vSecId2.some,
              vUserId,
              vCollId1,
              "type-1",
              "title-1",
              "content-1",
              "iv-1",
              List("h1", "h2"),
              now,
              now
            )
          ).drop(offset).take(limit).pure[F]
        case _ => List.empty[Secret].pure[F]
      }

    override def totalBy(userId: UUID,
                         collectionId: UUID,
                         hashes: List[String]): F[Int] =
      (userId.toString, collectionId.toString, hashes) match {
        case (
            "643ecddf-5823-4c6c-8625-8183b1d30c9a",
            "38ec60a1-b255-4714-9022-0ebbe4abcaaa",
            List("h1")
            ) =>
          2.pure[F]
        case (
            "643ecddf-5823-4c6c-8625-8183b1d30c9a",
            "38ec60a1-b255-4714-9022-0ebbe4abcaaa",
            List("h2")
            ) =>
          1.pure[F]
        case (
            "643ecddf-5823-4c6c-8625-8183b1d30c9a",
            "38ec60a1-b255-4714-9022-0ebbe4abcaaa",
            List("h1", "h2")
            ) =>
          1.pure[F]
        case _ => 0.pure[F]
      }
  }
}
