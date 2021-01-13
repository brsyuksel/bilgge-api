package bilgge.login.services

import java.util.UUID
import scala.concurrent.duration._

import cats.effect._
import cats.effect.testing.scalatest.AsyncIOSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.freespec.AsyncFreeSpec

import bilgge.login._
import bilgge.common._

class CirceJWTokenSpec extends AsyncFreeSpec with AsyncIOSpec with Matchers {
  val t = new CirceJWToken[IO]("secret-key", 120) {}

  "sign" - {
    "returns a token as string" in {
      t.sign(Claim(UUID.randomUUID(), "ybaroj")).asserting { t =>
        t.split('.').toList.size.shouldBe(3)
      }
    }
  }

  "verify" - {
    "returns token is not valid error if token is expired" in {
      val shortLive = new CirceJWToken[IO]("secret-key", 0) {}
      val res = for {
        token <- shortLive.sign(Claim(UUID.randomUUID(), "ybaroj"))
        _ <- IO.sleep(10.milliseconds)
        _ <- shortLive.verify(token)
      } yield ()

      res.attempt.asserting { e =>
        val err = e.swap.map(_.asInstanceOf[BilggeException])
        err.map(_.reason).shouldBe(Right(Reason.Authentication))
        err.map(_.messages).shouldBe(Right(List("token is not valid")))
      }
    }
    "returns claim data" in {
      val c = Claim(UUID.randomUUID(), "ybaroj")
      val res = for {
        token <- t.sign(c)
        claim <- t.verify(token)
      } yield claim

      res.asserting { cl =>
        cl.userId.shouldBe(c.userId)
        cl.username.shouldBe(c.username)
      }
    }
  }
}
