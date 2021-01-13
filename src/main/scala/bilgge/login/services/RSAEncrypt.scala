package bilgge.login.services

import java.security._
import java.security.spec.X509EncodedKeySpec
import org.apache.commons.codec.binary.Base64
import javax.crypto._

import cats.implicits._
import cats.effect._

import bilgge.login.Encrypt

abstract class RSAEncrypt[F[_]: Sync] extends Encrypt[F] {
  private def decodeKey(key: String): F[Array[Byte]] =
    Sync[F].delay(new Base64().decode(key))

  private def publicKey(key: Array[Byte]): F[PublicKey] =
    Sync[F].delay {
      val spec = new X509EncodedKeySpec(key)
      val factory = KeyFactory.getInstance("RSA")
      factory.generatePublic(spec)
    }

  private def encryptPlain(publicKey: PublicKey,
                           plain: String): F[Array[Byte]] =
    Sync[F].delay {
      val cipher = Cipher.getInstance("RSA")
      cipher.init(Cipher.ENCRYPT_MODE, publicKey)
      cipher.doFinal(plain.getBytes)
    }

  private def encodeB64(cipherText: Array[Byte]): F[String] =
    Sync[F].delay(new Base64().encodeAsString(cipherText))

  override def encrypt(pubKey: String, plain: String): F[String] =
    for {
      keyBytes <- decodeKey(pubKey)
      pKey <- publicKey(keyBytes)
      cipherText <- encryptPlain(pKey, plain)
      encoded <- encodeB64(cipherText)
    } yield encoded
}
