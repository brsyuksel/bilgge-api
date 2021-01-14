package bilgge

import cats.effect._
import cats.implicits._
import doobie._
import doobie.implicits._
import doobie.hikari._
import io.circe.generic.auto._
import io.circe.config.parser

import bilgge.users.repo.DoobieUserRepository
import bilgge.collections.repo.DoobieCollectionRepository
import bilgge.secrets.repo.DoobieSecretRepository
import bilgge.register._
import bilgge.login._
import bilgge.login.services._
import bilgge.collections._
import bilgge.secrets._

case class Http(host: String, port: Int)
case class DB(uri: String, user: String, password: String, connections: Int)
case class Secrets(jwt: String, hash: String)
case class Config(http: Http, db: DB, secrets: Secrets)

object main extends IOApp {
  def transactor(db: DB): Resource[IO, HikariTransactor[IO]] =
    for {
      ce <- ExecutionContexts.fixedThreadPool[IO](db.connections)
      be <- Blocker[IO]
      xa <- HikariTransactor.newHikariTransactor[IO](
        "org.postgresql.Driver",
        db.uri,
        db.user,
        db.password,
        ce,
        be
      )
    } yield xa

  override def run(args: List[String]): IO[ExitCode] =
    IO.fromEither(parser.decode[Config]()) >>= { conf =>
      transactor(conf.db).use { xa =>
        val userRepo = new DoobieUserRepository[IO](xa) {}
        val collRepo = new DoobieCollectionRepository[IO](xa) {}
        val secretRepo = new DoobieSecretRepository[IO](xa) {}

        val randomStringGenerator = new RandomStringGenerator[IO] {}
        val hashGenerator = new SHA256HashGenerator[IO] {}
        val jwtToken = new CirceJWToken[IO](conf.secrets.jwt, 21600) {}
        val encrypt = new RSAEncrypt[IO] {}

        val registerModule = new RegisterModule[IO](userRepo) {}
        val loginModule = new LoginModule[IO](
          conf.secrets.hash,
          userRepo,
          randomStringGenerator,
          encrypt,
          hashGenerator,
          jwtToken
        ) {}
        val collModule = new CollectionsModule[IO](userRepo, collRepo) {}
        val secModule = new SecretsModule[IO](userRepo, collRepo, secretRepo) {}

        val h = new http(
          jwtToken,
          registerModule,
          loginModule,
          collModule,
          secModule
        ) {}
        h.build(conf.http.host, conf.http.port).as(ExitCode.Success)
      }
    }
}
