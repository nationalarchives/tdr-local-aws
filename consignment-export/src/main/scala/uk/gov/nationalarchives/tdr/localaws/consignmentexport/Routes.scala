package uk.gov.nationalarchives.tdr.localaws.consignmentexport

import java.util.{Date, UUID}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.Credentials
import com.nimbusds.oauth2.sdk.token.BearerAccessToken
import com.typesafe.config.Config
import graphql.codegen.GetConsignment.{getConsignment => gc}
import graphql.codegen.UpdateExportData.{updateExportData => ued}
import graphql.codegen.types.UpdateExportDataInput
import io.circe.generic.auto._
import io.circe.syntax._
import sttp.client3.{HttpClientFutureBackend, SttpBackend}
import uk.gov.nationalarchives.tdr.GraphQLClient
import uk.gov.nationalarchives.tdr.keycloak.{KeycloakUtils, TdrKeycloakDeployment}

import java.time.ZonedDateTime
import scala.concurrent.{ExecutionContext, Future}

class Routes(config: Config)(implicit val executionContext: ExecutionContext) {

  case class Response(executionArn: String, startDate: Long)

  val getConsignmentClient = new GraphQLClient[gc.Data, gc.Variables](config.getString("api.baseUrl"))
  val updateExportDataClient = new GraphQLClient[ued.Data, ued.Variables](config.getString("api.baseUrl"))

  implicit val sttpBackend: SttpBackend[Future, Any] = HttpClientFutureBackend()
  implicit val tdrKeycloakDeployment: TdrKeycloakDeployment = TdrKeycloakDeployment(config.getString("auth.baseUrl"), "tdr", 3600)

  private val keycloakUtils: KeycloakUtils = KeycloakUtils()
  private val clientId = config.getString("auth.client.id")
  private val clientSecret = config.getString("auth.client.secret")

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
      authenticateOAuth2Async("tdr", authenticator) { _ =>
        complete(
          for {
            token <- keycloakUtils.serviceAccountToken(clientId, clientSecret)
            res <- updateExportDataClient.getResult(token, ued.document, Option(ued.Variables(UpdateExportDataInput(consignmentId, s"s3://fakeBucket/$consignmentId.tar.gz", Option(ZonedDateTime.now()), "v1"))))
              .map(_ => Response("executionArn", new Date().getTime).asJson.noSpaces)
          } yield res
        )
      }
    }
  }
}
