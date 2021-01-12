package bilgge.secrets

import java.util.UUID

import cats.effect._
import cats.effect.testing.scalatest.AsyncIOSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.freespec.AsyncFreeSpec

import bilgge.mock

import bilgge.common._

class SecretsModuleSpec extends AsyncFreeSpec with AsyncIOSpec with Matchers {
  val u = new mock.HCUserRepository[IO]
  val c = new mock.HCCollectionRepository[IO]
  val s = new mock.HCSecretRepository[IO]
  val module = new SecretsModule[IO](u, c, s) {}

  "create" - {
    "validates inputs" in {
      module
        .create(UUID.randomUUID(), UUID.randomUUID(), "", "", "", "", Nil)
        .attempt
        .asserting { e =>
          e.isLeft.shouldBe(true)
          val err = e.swap.map(_.asInstanceOf[BilggeException])
          err
            .map(_.reason)
            .shouldBe(Right(Reason.Validation))
          err
            .map(_.messages.size)
            .shouldBe(Right(5))
        }
    }
    "returns user not found error" in {
      module
        .create(
          UUID.randomUUID(),
          UUID.randomUUID(),
          "type",
          "title",
          "content",
          "iv",
          List("h1")
        )
        .attempt
        .asserting { e =>
          val err = e.swap.map(_.asInstanceOf[BilggeException])
          err.map(_.reason).shouldBe(Right(Reason.NotFound))
          err.map(_.messages).shouldBe(Right(List("user not found")))
        }
    }
    "returns collection not found error" in {
      module
        .create(
          mock.vUserId,
          UUID.randomUUID(),
          "type",
          "title",
          "content",
          "iv",
          List("h1")
        )
        .attempt
        .asserting { e =>
          val err = e.swap.map(_.asInstanceOf[BilggeException])
          err.map(_.reason).shouldBe(Right(Reason.NotFound))
          err.map(_.messages).shouldBe(Right(List("collection not found")))
        }
    }
    "returns created secret" in {
      module
        .create(
          mock.vUserId,
          mock.vCollId1,
          "type",
          "title",
          "content",
          "iv",
          List("h1")
        )
        .asserting { s =>
          s.id.nonEmpty.shouldBe(true)
          s.createdAt.nonEmpty.shouldBe(true)
          s.updatedAt.nonEmpty.shouldBe(true)
        }
    }
  }

  "update" - {
    "validates inputs" in {
      module
        .update(
          UUID.randomUUID(),
          UUID.randomUUID(),
          UUID.randomUUID(),
          "",
          "",
          "",
          "",
          Nil
        )
        .attempt
        .asserting { e =>
          e.isLeft.shouldBe(true)
          val err = e.swap.map(_.asInstanceOf[BilggeException])
          err
            .map(_.reason)
            .shouldBe(Right(Reason.Validation))
          err
            .map(_.messages.size)
            .shouldBe(Right(5))
        }

    }
    "returns user not found error" in {
      module
        .update(
          UUID.randomUUID(),
          UUID.randomUUID(),
          UUID.randomUUID(),
          "type",
          "title",
          "content",
          "iv",
          List("h1")
        )
        .attempt
        .asserting { e =>
          val err = e.swap.map(_.asInstanceOf[BilggeException])
          err.map(_.reason).shouldBe(Right(Reason.NotFound))
          err.map(_.messages).shouldBe(Right(List("user not found")))
        }

    }
    "returns collection not found error" in {
      module
        .update(
          mock.vUserId,
          UUID.randomUUID(),
          UUID.randomUUID(),
          "type",
          "title",
          "content",
          "iv",
          List("h1")
        )
        .attempt
        .asserting { e =>
          val err = e.swap.map(_.asInstanceOf[BilggeException])
          err.map(_.reason).shouldBe(Right(Reason.NotFound))
          err.map(_.messages).shouldBe(Right(List("collection not found")))
        }
    }
    "returns secret not found error" in {
      module
        .update(
          mock.vUserId,
          UUID.randomUUID(),
          mock.vCollId1,
          "type",
          "title",
          "content",
          "iv",
          List("h1")
        )
        .attempt
        .asserting { e =>
          val err = e.swap.map(_.asInstanceOf[BilggeException])
          err.map(_.reason).shouldBe(Right(Reason.NotFound))
          err.map(_.messages).shouldBe(Right(List("secret not found")))
        }
    }
    "returns updated secret" in {
      module
        .update(
          mock.vUserId,
          mock.vSecId1,
          mock.vCollId1,
          "type",
          "title",
          "content",
          "iv",
          List("h1")
        )
        .assertNoException
    }
  }

  "delete" - {
    "returns user not found error" in {
      module.delete(UUID.randomUUID(), UUID.randomUUID()).attempt.asserting {
        e =>
          val err = e.swap.map(_.asInstanceOf[BilggeException])
          err.map(_.reason).shouldBe(Right(Reason.NotFound))
          err.map(_.messages).shouldBe(Right(List("user not found")))
      }
    }
    "returns secret not found error" in {
      module.delete(mock.vUserId, UUID.randomUUID()).attempt.asserting { e =>
        val err = e.swap.map(_.asInstanceOf[BilggeException])
        err.map(_.reason).shouldBe(Right(Reason.NotFound))
        err.map(_.messages).shouldBe(Right(List("secret not found")))
      }
    }
    "returns unit after successful delete" in {
      module.delete(mock.vUserId, mock.vSecId1).assertNoException
    }
  }

  "get" - {
    "returns user not found error" in {
      module.get(UUID.randomUUID(), UUID.randomUUID()).attempt.asserting { e =>
        val err = e.swap.map(_.asInstanceOf[BilggeException])
        err.map(_.reason).shouldBe(Right(Reason.NotFound))
        err.map(_.messages).shouldBe(Right(List("user not found")))
      }
    }
    "returns secret not found error" in {
      module.get(mock.vUserId, UUID.randomUUID()).attempt.asserting { e =>
        val err = e.swap.map(_.asInstanceOf[BilggeException])
        err.map(_.reason).shouldBe(Right(Reason.NotFound))
        err.map(_.messages).shouldBe(Right(List("secret not found")))
      }
    }
    "returns secret" in {
      module.get(mock.vUserId, mock.vSecId1).asserting { s =>
        s.title.shouldBe("title-1")
      }
    }
  }

  "list" - {
    "returns user not found error" in {
      module
        .list(UUID.randomUUID(), UUID.randomUUID(), List.empty[String], 0, 10)
        .attempt
        .asserting { e =>
          val err = e.swap.map(_.asInstanceOf[BilggeException])
          err.map(_.reason).shouldBe(Right(Reason.NotFound))
          err.map(_.messages).shouldBe(Right(List("user not found")))
        }
    }
    "returns collection not found error" in {
      module
        .list(mock.vUserId, UUID.randomUUID(), List.empty[String], 0, 10)
        .attempt
        .asserting { e =>
          val err = e.swap.map(_.asInstanceOf[BilggeException])
          err.map(_.reason).shouldBe(Right(Reason.NotFound))
          err.map(_.messages).shouldBe(Right(List("collection not found")))
        }

    }
    "returns total and secrets list" in {
      module
        .list(mock.vUserId, mock.vCollId1, List("h1"), 0, 1)
        .asserting { r =>
          r.total.shouldBe(2)
          r.data.size.shouldBe(1)
        }
    }
  }
}
