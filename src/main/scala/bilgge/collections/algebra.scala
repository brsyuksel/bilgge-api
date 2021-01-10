package bilgge.collections

import java.util.UUID
import java.time.LocalDateTime

final case class Collection(id: Option[UUID] = None,
                            userId: UUID,
                            name: String,
                            iv: String,
                            createdAt: Option[LocalDateTime] = None,
                            updatedAt: Option[LocalDateTime] = None)

trait CollectionRepository[F[_]] {
  def create(c: Collection): F[Collection]
  def update(c: Collection): F[Collection]
  def delete(c: Collection): F[Unit]
  def getBy(id: UUID, userId: UUID): F[Option[Collection]]
  def listBy(userId: UUID): F[List[Collection]]
}
