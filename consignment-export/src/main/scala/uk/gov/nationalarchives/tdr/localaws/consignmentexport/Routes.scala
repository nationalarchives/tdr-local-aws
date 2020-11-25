package uk.gov.nationalarchives.tdr.localaws.consignmentexport

import java.util.{Date, UUID}

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.Credentials
import com.nimbusds.oauth2.sdk.token.BearerAccessToken
import com.typesafe.config.Config
import graphql.codegen.GetConsignment.{getConsignment => gc}
import graphql.codegen.UpdateExportLocation.{updateExportLocation => uel}
import graphql.codegen.types.UpdateExportLocationInput
import io.circe.generic.auto._
import io.circe.syntax._
import sttp.client.asynchttpclient.future.AsyncHttpClientFutureBackend
import sttp.client.{NothingT, SttpBackend}
import uk.gov.nationalarchives.tdr.GraphQLClient
import scala.concurrent.duration._

import scala.concurrent.{Await, ExecutionContext, Future}

class Routes(config: Config)(implicit val executionContext: ExecutionContext) {

  case class Response(executionArn: String, startDate: Long)

  val getConsignmentClient = new GraphQLClient[gc.Data, gc.Variables](config.getString("api.baseUrl"))
  val updateExportLocationClient = new GraphQLClient[uel.Data, uel.Variables](config.getString("api.baseUrl"))

  implicit val sttpBackend: SttpBackend[Future, Nothing, NothingT] = AsyncHttpClientFutureBackend()

  def tokenAuthenticator(credentials: Credentials, consignmentId: UUID): Future[Option[BearerAccessToken]] = {
    credentials match {
      case Credentials.Provided(token) =>
        getConsignmentClient.getResult(new BearerAccessToken(token), gc.document, Option(gc.Variables(consignmentId)))
          .map(r =>
            for {
              data <- r.data
              _ <- data.getConsignment
            } yield new BearerAccessToken(token)
          )
      case _ => Future.successful(None)
    }
  }

  val route: Route = pathPrefix("export") {
    path(JavaUUID) { consignmentId =>
      val authenticator = tokenAuthenticator(_, consignmentId)
      authenticateOAuth2Async("tdr", authenticator) { accessToken =>
        Await.result(updateExportLocationClient.getResult(accessToken, uel.document, Option(uel.Variables(UpdateExportLocationInput(consignmentId, s"s3://fakeBucket/$consignmentId.tar.gz"))))
          .map(_ => complete(Response("executionArn", new Date().getTime).asJson.noSpaces))
          .recover(e => failWith(e)), 1.seconds)
      }
    }
  }
}
