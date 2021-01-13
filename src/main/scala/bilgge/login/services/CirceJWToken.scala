package bilgge.login.services

import java.time.Instant

import cats.effect._
import cats.implicits._
import io.circe.{Decoder, Encoder}
import io.circe.syntax._
import io.circe.parser._
import pdi.jwt.{JwtClaim, JwtAlgorithm, JwtCirce}

import bilgge.login._
import bilgge.common._

abstract class CirceJWToken[F[_]: Sync](secret: String, expiresIn: Long)
    extends Token[F] {
  import CirceJWToken._

  private def nowPlusSec(n: Long): Long =
    Instant.now.plusSeconds(n).getEpochSecond

  override def sign(c: Claim): F[String] =
    Sync[F].delay {
      val content = c.asJson.noSpaces
      val jwtClaim =
        JwtClaim(content = content, expiration = nowPlusSec(expiresIn).some)
      JwtCirce.encode(jwtClaim, secret, JwtAlgorithm.HS256)
    }

  private def isValid(token: String): F[Boolean] =
    Sync[F].delay {
      JwtCirce.isValid(token, secret, Seq(JwtAlgorithm.HS256))
    }

  private def decodeToken(token: String): F[JwtClaim] =
    Sync[F]
      .fromTry {
        JwtCirce.decode(token, secret, Seq(JwtAlgorithm.HS256))
      }
      .adaptErr {
        case _ => BilggeException.authentication("token couldn't be decoded")
      }

  override def verify(token: String): F[Claim] =
    for {
      validation <- isValid(token)
      _ <- Sync[F].whenA(!validation) {
        Sync[F].raiseError(BilggeException.authentication("token is not valid"))
      }
      jwtClaim <- decodeToken(token)
      content = decode[Claim](jwtClaim.content).leftMap(
        _ => BilggeException.authentication("content couldn't be parsed")
      )
      claim <- Sync[F].fromEither(content)
    } yield claim

}

object CirceJWToken {
  implicit val enc: Encoder[Claim] = Encoder.forProduct2("user_id", "username")(
    c => (c.userId.toString, c.username)
  )

  implicit val dec: Decoder[Claim] =
    Decoder.forProduct2("user_id", "username")(Claim.apply)
}
