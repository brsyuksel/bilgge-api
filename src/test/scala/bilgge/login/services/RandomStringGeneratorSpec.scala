package bilgge.login.services

import cats.effect._
import cats.effect.testing.scalatest.AsyncIOSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.freespec.AsyncFreeSpec

class RandomStringGeneratorSpec
    extends AsyncFreeSpec
    with AsyncIOSpec
    with Matchers {

  val randStr = new RandomStringGenerator[IO] {}

  "generate" - {
    "generates string in length specified by user" in {
      val res = for {
        s1 <- randStr.generate(1)
        s2 <- randStr.generate(10)
        s3 <- randStr.generate(10)
      } yield (s1, s2, s3)

      res.asserting { t =>
        t._1.length.shouldBe(1)
        t._2.length.shouldBe(10)
        t._3.length.shouldBe(10)
        (t._2 == t._3).shouldBe(false)
      }
    }
  }
}
