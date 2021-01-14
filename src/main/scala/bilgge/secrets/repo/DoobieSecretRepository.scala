package bilgge.secrets.repo

import java.util.UUID
import java.time.LocalDateTime

import cats._
import cats.implicits._
import cats.effect._
import doobie._
import doobie.implicits._
import doobie.implicits.javatime._
import doobie.postgres.implicits._
import doobie.postgres.circe.jsonb.implicits._
import io.circe.syntax._

import bilgge.secrets._

abstract class DoobieSecretRepository[F[_]](xa: Transactor[F])(
  implicit B: Bracket[F, Throwable]
) extends SecretRepository[F] {
  import DoobieSecretRepository.stmt

  override def create(s: Secret): F[Secret] =
    stmt
      .create(s)
      .withUniqueGeneratedKeys[(UUID, LocalDateTime, LocalDateTime)](
        "id",
        "created_at",
        "updated_at"
      )
      .transact(xa)
      .map { t =>
        s.copy(id = t._1.some, createdAt = t._2.some, updatedAt = t._3.some)
      }

  override def update(s: Secret): F[Secret] =
    stmt
      .update(s)
      .withUniqueGeneratedKeys[LocalDateTime]("updated_at")
      .transact(xa)
      .map(d => s.copy(updatedAt = d.some))

  override def delete(s: Secret): F[Unit] =
    stmt.delete(s).run.transact(xa).void

  override def getBy(id: UUID, userId: UUID): F[Option[Secret]] =
    stmt.getBy(id, userId).transact(xa)

  override def listBy(userId: UUID,
                      collectionId: UUID,
                      hashes: List[String],
                      offset: Int,
                      limit: Int): F[List[Secret]] =
    stmt.listBy(userId, collectionId, hashes, offset, limit).transact(xa)

  override def totalBy(userId: UUID,
                       collectionId: UUID,
                       hashes: List[String]): F[Int] =
    stmt.totalBy(userId, collectionId, hashes).transact(xa)
}

object DoobieSecretRepository {
  private object stmt {
    def create(s: Secret) =
      sql"""
           INSERT INTO secrets (user_id, collection_id, type, title, content, hashes, iv)
           VALUES (${s.userId}, ${s.collectionId}, ${s.`type`}, ${s.title}, ${s.content}, ${s.hashes.asJson}, ${s.iv})
         """.update

    def update(s: Secret) =
      sql"""
           UPDATE secrets SET type = ${s.`type`}, title = ${s.title}, content = ${s.content}, 
           hashes = ${s.hashes.asJson}, iv = ${s.iv}, updated_at = current_timestamp
           WHERE id = ${s.id}
         """.update

    def delete(s: Secret) =
      sql"""DELETE FROM secrets WHERE id = ${s.id}""".update

    def getBy(id: UUID, userId: UUID) =
      sql"""
           SELECT id, user_id, collection_id, type, title, content, iv, ARRAY(SELECT jsonb_array_elements_text(hashes)), created_at, updated_at
           FROM secrets WHERE id = $id AND user_id = $userId
         """.query[Secret].option

    def listBy(userId: UUID,
               collectionId: UUID,
               hashes: List[String],
               offset: Int,
               limit: Int) =
      sql"""
           SELECT id, user_id, collection_id, type, title, content, iv, ARRAY(SELECT jsonb_array_elements_text(hashes)), created_at, updated_at
           FROM secrets WHERE user_id = $userId AND collection_id = $collectionId AND hashes @> ${hashes.asJson}
           OFFSET $offset LIMIT $limit
         """.query[Secret].to[List]

    def totalBy(userId: UUID, collectionId: UUID, hashes: List[String]) =
      sql"""
           SELECT COUNT(*)
           FROM secrets WHERE user_id = $userId AND collection_id = $collectionId AND hashes @> ${hashes.asJson}
         """.query[Int].unique
  }
}
