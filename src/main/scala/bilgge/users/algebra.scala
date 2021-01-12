package bilgge.users

import java.util.UUID
import java.time.LocalDateTime

final case class User(id: Option[UUID] = None,
                      username: String,
                      publicKey: String,
                      key: String,
                      salt: String,
                      loginToken: Option[String] = None,
                      createdAt: Option[LocalDateTime] = None,
                      updatedAt: Option[LocalDateTime] = None)

trait UserRepository[F[_]] {
  def create(u: User): F[User]
  def update(u: User): F[User]
  def getBy(username: String): F[Option[User]]
  def getBy(id: UUID): F[Option[User]]
}
