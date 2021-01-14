package bilgge

import java.util.UUID

import scala.concurrent.ExecutionContext

import cats.data._
import cats.effect._
import cats.implicits._
import org.http4s._
import org.http4s.headers.Authorization
import org.http4s.implicits._
import org.http4s.circe._
import org.http4s.dsl.io._
import org.http4s.server._
import org.http4s.server.blaze._
import io.circe._
import io.circe.generic.semiauto._
import io.circe.generic.auto._
import io.circe.syntax._

import bilgge.common._
import bilgge.register.RegisterModule
import bilgge.login.{Authorized, LoginModule, Claim, Token}
import bilgge.collections.{CollectionsModule, Collection}
import bilgge.secrets.{SecretsModule, Secret}

abstract class http(jwtToken: Token[IO],
                    registerModule: RegisterModule[IO],
                    loginModule: LoginModule[IO],
                    collectionsModule: CollectionsModule[IO],
                    secretsModule: SecretsModule[IO]) {
  def build(host: String, port: Int)(implicit C: ConcurrentEffect[IO],
                                     T: Timer[IO]): IO[Unit] =
    BlazeServerBuilder[IO](ExecutionContext.global)
      .bindHttp(port, host)
      .withHttpApp(router)
      .serve
      .compile
      .drain

  import http.requests._
  import http.responses._

  private def errorHandler: Throwable => IO[Response[IO]] = {
    case t: BilggeException =>
      val err = ErrorResponse(t.reason.toString, t.messages)
      BadRequest(err.asJson)
    case t => InternalServerError(t.getMessage)
  }

  implicit val uuidQPDec: QueryParamDecoder[UUID] =
    QueryParamDecoder[String].map(UUID.fromString)

  /**
    * POST /register
    */
  lazy val registerRoutes = HttpRoutes.of[IO] {
    case req @ POST -> Root / "register" =>
      val res = for {
        r <- req.as[RegisterRequest]
        _ <- registerModule.createUser(r.username, r.publicKey, r.key, r.salt)
        resp <- Created()
      } yield resp
      res.handleErrorWith(errorHandler)
  }

  /**
    * POST /login/request
    * POST /login/authorize
    */
  lazy val loginRoutes = HttpRoutes.of[IO] {
    case req @ POST -> Root / "login" / "request" =>
      val res = for {
        r <- req.as[LoginReqRequest]
        cipherText <- loginModule.request(r.username)
        body = LoginCipherTextResponse(cipherText).asJson
        resp <- Ok(body)
      } yield resp
      res.handleErrorWith(errorHandler)

    case req @ POST -> Root / "login" / "authenticate" =>
      val res = for {
        r <- req.as[LoginAuthRequest]
        auth <- loginModule.authorize(r.username, r.plain)
        resp <- Ok(auth.asJson)
      } yield resp
      res.handleErrorWith(errorHandler)
  }

  /**
    * GET /collections
    * POST /collections
    * PUT /collections/:uuid
    * DELETE /collections/:uuid
    */
  lazy val collectionRoutes = AuthedRoutes.of[Claim, IO] {
    case GET -> Root / "collections" as claim =>
      collectionsModule
        .list(claim.userId)
        .flatMap(l => Ok(l.asJson))
        .handleErrorWith(errorHandler)

    case req @ POST -> Root / "collections" as claim =>
      val res = for {
        r <- req.req.as[CollectionUpsertRequest]
        _ <- collectionsModule.create(claim.userId, r.name, r._iv)
        resp <- Created() // FIXME: location header
      } yield resp
      res.handleErrorWith(errorHandler)

    case req @ PUT -> Root / "collections" / UUIDVar(collId) as claim =>
      val res = for {
        r <- req.req.as[CollectionUpsertRequest]
        _ <- collectionsModule.update(claim.userId, collId, r.name, r._iv)
        resp <- NoContent()
      } yield resp
      res.handleErrorWith(errorHandler)

    case DELETE -> Root / "collections" / UUIDVar(collId) as claim =>
      collectionsModule
        .delete(claim.userId, collId)
        .flatMap(_ => NoContent())
        .handleErrorWith(errorHandler)
  }

  /**
    * GET /secrets
    * POST /secrets
    * GET /secrets/:uuid
    * PUT /secrets/:uuid
    * DELETE /secrets/:uuid
    */
  object collIdQPMatcher extends QueryParamDecoderMatcher[UUID]("collection_id")
  object offsetQPMatcher extends QueryParamDecoderMatcher[Int]("offset")
  object limitQPMatcher extends QueryParamDecoderMatcher[Int]("limit")
  object qQPOptMatcher extends OptionalQueryParamDecoderMatcher[String]("q")
  lazy val secretRoutes = AuthedRoutes.of[Claim, IO] {
    case GET -> Root / "secrets" :? collIdQPMatcher(collId) +&
          offsetQPMatcher(offset) +& limitQPMatcher(limit) +& qQPOptMatcher(h) as claim =>
      val q = h
        .map(_.split(',').toList.filter(_.nonEmpty))
        .getOrElse(List.empty[String])

      secretsModule
        .list(claim.userId, collId, q, offset, limit)
        .map(s => PaginatedData(Pagination(s.total, offset, limit), s.data))
        .flatMap(d => Ok(d.asJson))
        .handleErrorWith(errorHandler)

    case req @ POST -> Root / "secrets" as claim =>
      val res = for {
        r <- req.req.as[SecretUpsertRequest]
        _ <- secretsModule.create(
          claim.userId,
          r.collectionId,
          r.`type`,
          r.title,
          r.content,
          r.iv,
          r.hashes
        )
        resp <- Created() // FIXME: location header
      } yield resp
      res.handleErrorWith(errorHandler)

    case GET -> Root / "secrets" / UUIDVar(secretId) as claim =>
      secretsModule
        .get(claim.userId, secretId)
        .flatMap(s => Ok(s.asJson))
        .handleErrorWith(errorHandler)

    case req @ PUT -> Root / "secrets" / UUIDVar(secretId) as claim =>
      val res = for {
        r <- req.req.as[SecretUpsertRequest]
        _ <- secretsModule.update(
          claim.userId,
          secretId,
          r.collectionId,
          r.`type`,
          r.title,
          r.content,
          r.iv,
          r.hashes
        )
        resp <- NoContent()
      } yield resp
      res.handleErrorWith(errorHandler)

    case DELETE -> Root / "secrets" / UUIDVar(secretId) as claim =>
      secretsModule
        .delete(claim.userId, secretId)
        .flatMap(_ => NoContent())
        .handleErrorWith(errorHandler)
  }

  val authUser: Kleisli[IO, Request[IO], Either[Throwable, Claim]] =
    Kleisli({ req =>
      val res = for {
        header <- req.headers
          .get(Authorization)
          .toRight("token not found")
        token <- header.value
          .split(" ")
          .lastOption
          .toRight("malformed header")
      } yield token

      IO.fromEither(res.leftMap(m => BilggeException.authentication(m)))
        .flatMap(jwtToken.verify)
        .attempt
    })

  val onFailure: AuthedRoutes[Throwable, IO] = Kleisli(
    _ => OptionT.liftF(Forbidden())
  )

  lazy val auth = AuthMiddleware(authUser, onFailure)

  lazy val services =
    registerRoutes <+>
      loginRoutes <+>
      auth(collectionRoutes) <+>
      auth(secretRoutes)

  lazy val router = Router("/" -> services).orNotFound
}

object http {
  object requests {
    case class RegisterRequest(username: String,
                               publicKey: String,
                               key: String,
                               salt: String)
    implicit val registerRequestDec: Decoder[RegisterRequest] =
      Decoder.forProduct4("username", "public_key", "key", "salt")(
        RegisterRequest.apply
      )
    implicit val registerRequestJsonOf: EntityDecoder[IO, RegisterRequest] =
      jsonOf[IO, RegisterRequest]

    case class LoginReqRequest(username: String)
    implicit val loginReqRequestJsonOf: EntityDecoder[IO, LoginReqRequest] =
      jsonOf[IO, LoginReqRequest]

    case class LoginAuthRequest(username: String, plain: String)
    implicit val loginAuthRequestJsonOf: EntityDecoder[IO, LoginAuthRequest] =
      jsonOf[IO, LoginAuthRequest]

    case class CollectionUpsertRequest(name: String, _iv: String)
    implicit val collUpsertJsonOf: EntityDecoder[IO, CollectionUpsertRequest] =
      jsonOf[IO, CollectionUpsertRequest]

    case class SecretUpsertRequest(collectionId: UUID,
                                   `type`: String,
                                   title: String,
                                   content: String,
                                   iv: String,
                                   hashes: List[String])
    implicit val secretUpsertReqEnc: Decoder[SecretUpsertRequest] =
      Decoder.forProduct6(
        "collection_id",
        "type",
        "title",
        "content",
        "_iv",
        "hashes"
      )(SecretUpsertRequest.apply)
    implicit val secretUpsertJsonOf: EntityDecoder[IO, SecretUpsertRequest] =
      jsonOf[IO, SecretUpsertRequest]
  }

  object responses {
    case class ErrorResponse(reason: String, messages: List[String])
    case class DataListingResponse[A: Encoder](data: A)
    case class Pagination(total: Int, offset: Int, limit: Int)
    case class PaginatedData[A: Encoder](pagination: Pagination, data: A)

    case class LoginCipherTextResponse(cipher: String)

    implicit val loginAuthEnc: Encoder[Authorized] =
      Encoder.forProduct4("token", "public_key", "key", "salt")(
        a => (a.token, a.publicKey, a.key, a.salt)
      )

    implicit val collectionEnc: Encoder[Collection] = Encoder.forProduct6(
      "id",
      "user_id",
      "name",
      "_iv",
      "created_at",
      "updated_at"
    )(c => (c.id, c.userId, c.name, c.iv, c.createdAt, c.updatedAt))

    implicit val secretEnc: Encoder[Secret] = Encoder.forProduct10(
      "id",
      "user_id",
      "collection_id",
      "type",
      "title",
      "content",
      "_iv",
      "hashes",
      "created_at",
      "updated_at"
    )(
      s =>
        (
          s.id,
          s.userId,
          s.collectionId,
          s.`type`,
          s.title,
          s.content,
          s.iv,
          s.hashes,
          s.createdAt,
          s.updatedAt
      )
    )
  }
}
