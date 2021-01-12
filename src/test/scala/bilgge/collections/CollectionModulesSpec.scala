package bilgge.collections

import java.util.UUID

import cats.effect._
import cats.effect.testing.scalatest.AsyncIOSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.freespec.AsyncFreeSpec

import bilgge.mock

import bilgge.common._

class CollectionModulesSpec
    extends AsyncFreeSpec
    with AsyncIOSpec
    with Matchers {
  val u = new mock.HCUserRepository[IO]
  val c = new mock.HCCollectionRepository[IO]
  val module = new CollectionsModule[IO](u, c) {}

  "create" - {
    "validates inputs" in {
      module.create(UUID.randomUUID(), "", "").attempt.asserting { e =>
        val err = e.swap.map(_.asInstanceOf[BilggeException])
        err.map(_.reason).shouldBe(Right(Reason.Validation))
        err.map(_.messages.size).shouldBe(Right(2))
      }
    }
    "returns user not found error" in {
      module.create(UUID.randomUUID(), "name", "iv").attempt.asserting { e =>
        val err = e.swap.map(_.asInstanceOf[BilggeException])
        err.map(_.reason).shouldBe(Right(Reason.NotFound))
        err.map(_.messages).shouldBe(Right(List("user not found")))
      }
    }
    "returns created collection" in {
      module.create(mock.vUserId, "name", "iv").asserting { c =>
        c.id.nonEmpty.shouldBe(true)
        c.createdAt.nonEmpty.shouldBe(true)
        c.updatedAt.nonEmpty.shouldBe(true)
      }
    }
  }

  "update" - {
    "validates inputs" in {
      module
        .update(UUID.randomUUID(), UUID.randomUUID(), "", "")
        .attempt
        .asserting { e =>
          val err = e.swap.map(_.asInstanceOf[BilggeException])
          err.map(_.reason).shouldBe(Right(Reason.Validation))
          err.map(_.messages.size).shouldBe(Right(2))
        }
    }
    "returns user not found error" in {
      module
        .update(UUID.randomUUID(), UUID.randomUUID(), "name", "iv")
        .attempt
        .asserting { e =>
          val err = e.swap.map(_.asInstanceOf[BilggeException])
          err.map(_.reason).shouldBe(Right(Reason.NotFound))
          err.map(_.messages).shouldBe(Right(List("user not found")))
        }
    }
    "returns collection not found error" in {
      module
        .update(mock.vUserId, UUID.randomUUID(), "name", "iv")
        .attempt
        .asserting { e =>
          val err = e.swap.map(_.asInstanceOf[BilggeException])
          err.map(_.reason).shouldBe(Right(Reason.NotFound))
          err.map(_.messages).shouldBe(Right(List("collection not found")))
        }

    }
    "returns updated collection" in {
      module.update(mock.vUserId, mock.vCollId1, "name", "iv").assertNoException
    }
  }

  "delete" - {
    "returns user not found error" in {
      module
        .delete(UUID.randomUUID(), UUID.randomUUID())
        .attempt
        .asserting { e =>
          val err = e.swap.map(_.asInstanceOf[BilggeException])
          err.map(_.reason).shouldBe(Right(Reason.NotFound))
          err.map(_.messages).shouldBe(Right(List("user not found")))
        }
    }
    "returns collection not found error" in {
      module
        .delete(mock.vUserId, UUID.randomUUID())
        .attempt
        .asserting { e =>
          val err = e.swap.map(_.asInstanceOf[BilggeException])
          err.map(_.reason).shouldBe(Right(Reason.NotFound))
          err.map(_.messages).shouldBe(Right(List("collection not found")))
        }
    }
    "returns unit after successful delete" in {
      module.delete(mock.vUserId, mock.vCollId1).assertNoException
    }
  }

  "list" - {
    "returns user not found error" in {
      module.list(UUID.randomUUID()).attempt.asserting { e =>
        val err = e.swap.map(_.asInstanceOf[BilggeException])
        err.map(_.reason).shouldBe(Right(Reason.NotFound))
        err.map(_.messages).shouldBe(Right(List("user not found")))
      }
    }
    "returns collection list by user" in {
      module.list(mock.vUserId).asserting(l => l.size.shouldBe(2))
    }
  }
}
