package bilgge.login

import cats.effect._
import cats.effect.testing.scalatest.AsyncIOSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.freespec.AsyncFreeSpec

import bilgge.mock
import bilgge.common._
import bilgge.users._

class LoginModuleSpec extends AsyncFreeSpec with AsyncIOSpec with Matchers {
  object userRepo extends mock.HCUserRepository[IO] {
    var lastUpdated: Option[User] = None

    override def update(u: User): IO[User] = {
      lastUpdated = Some(u)
      super.update(u)
    }
  }
  val strGen: StringGenerator[IO] = _ => IO("randStr")
  val enc: Encrypt[IO] = (_, p) => IO(s"ENC($p)")
  val hash: HashGenerator[IO] = {
    case "master-plain" => IO("login-token-hash-1")
    case p              => IO(s"$p")
  }
  val jwt: Token[IO] = new Token[IO] {
    override def sign(c: Claim): IO[String] = IO("jwt")

    override def verify(token: String): IO[Claim] =
      IO.raiseError(new NotImplementedError)
  }
  val module =
    new LoginModule[IO](userRepo, strGen, enc, hash, jwt) {}

  "request" - {
    "returns user not found error" in {
      module.request("non-existing").attempt.asserting { e =>
        val err = e.swap.map(_.asInstanceOf[BilggeException])
        err.map(_.reason).shouldBe(Right(Reason.NotFound))
        err.map(_.messages).shouldBe(Right(List("user not found")))
      }
    }
    "returns cipher" in {
      module.request("case-1").asserting { c =>
        c.shouldBe("ENC(randStr)")
        userRepo.lastUpdated
          .flatMap(_.loginToken)
          .shouldBe(Some("randStr"))
      }
    }
  }

  "authorize" - {
    "returns user not found error" in {
      module.authorize("non-existing", "plain").attempt.asserting { e =>
        val err = e.swap.map(_.asInstanceOf[BilggeException])
        err.map(_.reason).shouldBe(Right(Reason.NotFound))
        err.map(_.messages).shouldBe(Right(List("user not found")))
      }

    }
    "returns perform login request first error" in {
      module.authorize("case-2", "plain").attempt.asserting { e =>
        val err = e.swap.map(_.asInstanceOf[BilggeException])
        err.map(_.reason).shouldBe(Right(Reason.NotFound))
        err.map(_.messages).shouldBe(Right(List("perform login request first")))
      }

    }
    "returns plain does not match error" in {
      module.authorize("case-1", "plain").attempt.asserting { e =>
        val err = e.swap.map(_.asInstanceOf[BilggeException])
        err.map(_.reason).shouldBe(Right(Reason.Validation))
        err.map(_.messages).shouldBe(Right(List("plain does not match")))
      }
    }
    "returns authorized" in {
      module.authorize("case-1", "master-plain").asserting { a =>
        a.publicKey.shouldBe("pubkey-1")
        a.key.shouldBe("aeskey-1")
        a.salt.shouldBe("hashsalt-1")
        a.token.shouldBe("jwt")
      }
    }
  }
}
