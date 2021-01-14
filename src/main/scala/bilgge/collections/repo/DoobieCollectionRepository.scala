package bilgge.collections.repo

import java.util.UUID
import java.time.LocalDateTime

import cats._
import cats.implicits._
import cats.effect._
import doobie._
import doobie.implicits._
import doobie.implicits.javatime._
import doobie.postgres.implicits._

import bilgge.collections._

abstract class DoobieCollectionRepository[F[_]](xa: Transactor[F])(
  implicit B: Bracket[F, Throwable]
) extends CollectionRepository[F] {
  import DoobieCollectionRepository.stmt

  override def create(c: Collection): F[Collection] =
    stmt
      .create(c)
      .withUniqueGeneratedKeys[(UUID, LocalDateTime, LocalDateTime)](
        "id",
        "created_at",
        "updated_at"
      )
      .transact(xa)
      .map { t =>
        c.copy(id = t._1.some, createdAt = t._2.some, updatedAt = t._3.some)
      }

  override def update(c: Collection): F[Collection] =
    stmt
      .update(c)
      .withUniqueGeneratedKeys[LocalDateTime]("updated_at")
      .transact(xa)
      .map(d => c.copy(updatedAt = d.some))

  override def delete(c: Collection): F[Unit] =
    stmt.delete(c).run.transact(xa).void

  override def getBy(id: UUID, userId: UUID): F[Option[Collection]] =
    stmt.getBy(id, userId).transact(xa)

  override def listBy(userId: UUID): F[List[Collection]] =
    stmt.listBy(userId).transact(xa)
}

object DoobieCollectionRepository {
  private object stmt {
    def create(c: Collection) =
      sql"""
           INSERT INTO collections (user_id, name, iv)
           VALUES (${c.userId}, ${c.name}, ${c.iv})
         """.update

    def update(c: Collection) =
      sql"""
           UPDATE collections SET name = ${c.name}, iv = ${c.iv}, updated_at = current_timestamp
           WHERE id = ${c.id}
         """.update

    def delete(c: Collection) =
      sql"""DELETE FROM collections WHERE id = ${c.id}""".update

    def getBy(id: UUID, userId: UUID) =
      sql"""
           SELECT id, user_id, name, iv, created_at, updated_at
           FROM collections WHERE id = $id AND user_id = $userId
         """.query[Collection].option

    def listBy(userId: UUID) =
      sql"""
           SELECT id, user_id, name, iv, created_at, updated_at
           FROM collections WHERE user_id = $userId
         """.query[Collection].to[List]
  }
}
