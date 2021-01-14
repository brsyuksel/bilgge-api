package bilgge.users.repo

import java.util.UUID
import java.time.LocalDateTime

import cats._
import cats.implicits._
import cats.effect._
import doobie._
import doobie.implicits._
import doobie.implicits.javatime._
import doobie.postgres.implicits._

import bilgge.users._

abstract class DoobieUserRepository[F[_]](xa: Transactor[F])(
  implicit B: Bracket[F, Throwable]
) extends UserRepository[F] {
  import DoobieUserRepository.stmt

  override def create(u: User): F[User] =
    stmt
      .create(u)
      .withUniqueGeneratedKeys[(UUID, LocalDateTime, LocalDateTime)](
        "id",
        "created_at",
        "updated_at"
      )
      .transact(xa)
      .map { t =>
        u.copy(id = t._1.some, createdAt = t._2.some, updatedAt = t._3.some)
      }

  override def update(u: User): F[User] =
    stmt
      .update(u)
      .withUniqueGeneratedKeys[LocalDateTime]("updated_at")
      .transact(xa)
      .map(d => u.copy(updatedAt = d.some))

  override def getBy(id: UUID): F[Option[User]] =
    stmt.getBy(id).transact(xa)

  override def getBy(username: String): F[Option[User]] =
    stmt.getBy(username).transact(xa)
}

object DoobieUserRepository {
  private object stmt {
    def create(u: User) =
      sql"""
           INSERT INTO users (username, public_key, key, salt)
           VALUES (${u.username}, ${u.publicKey}, ${u.key}, ${u.salt})
         """.update

    def update(u: User) =
      sql"""
           UPDATE users SET login_token = ${u.loginToken}, updated_at = current_timestamp
           WHERE id = ${u.id}
         """.update

    def getBy(id: UUID) =
      sql"""
           SELECT id, username, public_key, key, salt, login_token, created_at, updated_at
           FROM users WHERE id = $id
         """.query[User].option

    def getBy(username: String) =
      sql"""
           SELECT id, username, public_key, key, salt, login_token, created_at, updated_at
           FROM users WHERE username = $username
         """.query[User].option
  }
}
