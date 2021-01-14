package bilgge.register

import cats.effect._
import cats.effect.testing.scalatest.AsyncIOSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.freespec.AsyncFreeSpec

import bilgge.mock

import bilgge.common._

class RegisterModuleSpec extends AsyncFreeSpec with AsyncIOSpec with Matchers {
  val u = new mock.HCUserRepository[IO]
  val module = new RegisterModule[IO](u) {}

  "createUser" - {
    "validates inputs" in {
      module.createUser("--", "", "", "").attempt.asserting { e =>
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
    "returns user already exists" in {
      module
        .createUser("case-1", "pubkey", "aeskey", "salt")
        .attempt
        .asserting { e =>
          val err = e.swap.map(_.asInstanceOf[BilggeException])
          err.map(_.reason).shouldBe(Right(Reason.Validation))
          err.map(_.messages).shouldBe(Right(List("user already exists")))
        }
    }
    "returns created user" in {
      module
        .createUser("ybaroj", "pubkey", "aeskey", "salt")
        .asserting { u =>
          u.id.nonEmpty.shouldBe(true)
          u.username.shouldBe("ybaroj")
        }
    }
  }
}
