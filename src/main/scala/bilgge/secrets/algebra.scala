package bilgge.secrets

import java.util.UUID
import java.time.LocalDateTime

final case class Secret(id: Option[UUID] = None,
                        userId: UUID,
                        collectionId: UUID,
                        `type`: String,
                        title: String,
                        content: String,
                        iv: String,
                        hashes: List[String],
                        createdAt: Option[LocalDateTime] = None,
                        updatedAt: Option[LocalDateTime] = None)

trait SecretRepository[F[_]] {
  def create(s: Secret): F[Secret]
  def update(s: Secret): F[Secret]
  def delete(s: Secret): F[Unit]
  def getBy(id: UUID, userId: UUID): F[Option[Secret]]
  def listBy(userId: UUID,
             collectionId: UUID,
             hashes: List[String],
             offset: Int,
             limit: Int): F[List[Secret]]
  def totalBy(userId: UUID, collectionId: UUID, hashes: List[String]): F[Int]
}
