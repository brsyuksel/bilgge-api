package bilgge.login.services

import cats.effect._
import cats.effect.testing.scalatest.AsyncIOSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.freespec.AsyncFreeSpec

class SHA256HashGeneratorSpec
    extends AsyncFreeSpec
    with AsyncIOSpec
    with Matchers {
  val gen = new SHA256HashGenerator[IO]("salty") {}

  "hash" - {
    "generates correct sha256 hashes" in {
      val res = for {
        h1 <- gen.hash("test-1")
        h2 <- gen.hash("test-2")
        h3 <- gen.hash("ybaroj")
      } yield (h1, h2, h3)

      res.asserting { t =>
        t._1.shouldBe(
          "6fd50e0ee8a98ff6aea75f1d1596267062717c52b9727be066c822e168e60935"
        )
        t._2.shouldBe(
          "fb5eec19e4543abcf9113f3d8eb14c0cd623bf4d905d604c16e4ea00f4257081"
        )
        t._3.shouldBe(
          "d261ea4902fade611b4113cab2858dbd71aa39c6642ed03f00c03357a9aed93f"
        )
      }
    }
  }
}
