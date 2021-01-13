package bilgge.login.services

import cats.effect._
import cats.effect.testing.scalatest.AsyncIOSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.freespec.AsyncFreeSpec

class RSAEncryptSpec extends AsyncFreeSpec with AsyncIOSpec with Matchers {
  val enc = new RSAEncrypt[IO] {}

  "encrypt" - {
    "performs successfully with 1024bit keys" in {
      val key =
        """
          |MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDQz6VKgolHspvCWBviSPeh70pS
          |jPQTpr98ZZe+vC0iG0PJrStPpIPfJuHLKoBsd0MyX4EEGB8+kzwpd7fyG6rXzMZ3
          |0BOWB9ELLKmCqCuK5fKmhaM4muBGQv4xssadkzmVNQhZivN3JvguvVHP//2lTlA3
          |7D0vRrnrL2oVjHCUCwIDAQAB
          |""".stripMargin

      enc.encrypt(key, "plain-text").assertNoException
    }
    "performs successfully with 2048bit keys" in {
      val key =
        """
          |MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA3yTWUlXnePerN3yactdX
          |VwxTq28NpAAhrZLwfX+xsNvEiO7aNVZtNiB6FioRPJrl11rTDFl/ksHoE2Lxnett
          |BZ+mpOZT8QT0tPSFI/VEFNLYwC6Q2Xz5zmj+TNtpkL01argL8XTMg7qoVKX3gULe
          |xJOoAGyRneHL8m0RCFN4k3wLASnQ5h4YVfYBlO4y1uIZBQ0LdHiKG36nUfk4mUEG
          |ZacG+65Jd8Sy2i2mLZmn3WDJPbu7XzJ45xRdVg8fWYUc9jNGZCo+H3alQWtw2SIx
          |zAjRXycThoDyLdA6ro/WYJ0Rvle3+olDUDPg3qyyuCftt4O8nugovdCoTWggDUOd
          |4wIDAQAB
          |""".stripMargin

      enc.encrypt(key, "plain-text").assertNoException
    }
    "performs successfully with 4096bit keys" in {
      val key =
        """
          |MIICITANBgkqhkiG9w0BAQEFAAOCAg4AMIICCQKCAgBVOMIQXrynWSd6VTOPZEtP
          |aiH/zObvamzZwBtoTFIML55Vd42an4jPcyMHmqWMPand0cI17K4fmuM18WmKSfVY
          |Exibph7cjxaddfYSd5TmxGH5mewxNeoR31wm2nKCgrMMJmzMalrcg2MGjM9Igsns
          |+VVOOam+nGhnPmedDF2R6MSyqhlbqrO2KbHQTivtSf5Vf1WZBql0VfPf9xAqsdDl
          |E9FtCiwBr6WypQrRwI3KDzBDcDeMttcnqxPFOy6WhotMJG9WnZ5Hw1BHWsZtk8/4
          |yLlXIzV1tGOoUKkgH1t7RCUYlrLhp7vFiWmqsbDsVyBrUE+f10G+Vxc9WGr2cbSh
          |FwCDD/XRfvQbY2qesDwl3hDY1UPD8T6SQr1C4Ko678W9LJa/4fW55LLpS1DJ9F4v
          |1vmTZWGPZaxuGYm5C8AA+jpjsezUMYhw9ur5Ac1ss0LNyDGBspzRP52shd/CWiM1
          |i5gSEzS10nYQ4MxNr+q5LYyuNJCsZYZdiEHIJuWlZAQphAQKb6iodLP2d+lN/vqf
          |WsugVwhgFL4WprzkzpcSFKEnjk2oNdA72+lfnMylpcTqKtv1Bc8/IaJA56fKqdvC
          |HoMH2hVfMoPteU67JorJBWZJHw61w8lzCfAc8exLNfg6vM/6b8+QxMObC928jD52
          |uAmVdSBlGTsMhasSU7qNiQIDAQAB
          |""".stripMargin

      enc.encrypt(key, "plain-text").assertNoException
    }
  }
}
