package bilgge.login.services

import cats.effect._
import cats.effect.testing.scalatest.AsyncIOSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.freespec.AsyncFreeSpec

class SHA256HashGeneratorSpec
    extends AsyncFreeSpec
    with AsyncIOSpec
    with Matchers {
  val gen = new SHA256HashGenerator[IO] {}

  "hash" - {
    "generates correct sha256 hashes" in {
      val res = for {
        h1 <- gen.hash("test-1", "salt-free")
        h2 <- gen.hash("test-2", "salty")
        h3 <- gen.hash("ybaroj", "")
      } yield (h1, h2, h3)

      res.asserting { t =>
        t._1.shouldBe(
          "e715f56d599b19c9378d1f213bbca14da3f77508c516bcceb17e488f0fc34d5b"
        )
        t._2.shouldBe(
          "fb5eec19e4543abcf9113f3d8eb14c0cd623bf4d905d604c16e4ea00f4257081"
        )
        // sum of "ybaroj.", not "ybaroj"
        t._3.shouldBe(
          "4b6294a2714705ad0a6cf0134422ac6f3f5d47c0ccbae699fd656d974dd20ec8"
        )
      }
    }
  }
}
