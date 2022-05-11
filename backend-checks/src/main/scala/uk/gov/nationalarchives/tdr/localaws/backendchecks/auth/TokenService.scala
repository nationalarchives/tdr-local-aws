package uk.gov.nationalarchives.tdr.localaws.backendchecks.auth

import com.nimbusds.oauth2.sdk.token.BearerAccessToken
import com.typesafe.config.Config
import sttp.client3.{HttpClientFutureBackend, SttpBackend}
import uk.gov.nationalarchives.tdr.keycloak.{KeycloakUtils, TdrKeycloakDeployment}

import scala.concurrent.{ExecutionContext, Future}

class TokenService(config: Config)(implicit val executionContext: ExecutionContext) {

  implicit private val backend: SttpBackend[Future, Any] = HttpClientFutureBackend()
  implicit val keycloakDeployment: TdrKeycloakDeployment = TdrKeycloakDeployment(config.getString("auth.baseUrl"), "tdr", 3600)

  private val keycloakUtils: KeycloakUtils = KeycloakUtils()
  private val clientId = config.getString("auth.client.id")
  private val clientSecret = config.getString("auth.client.secret")

  def token: Future[BearerAccessToken] = keycloakUtils.serviceAccountToken(clientId, clientSecret)
}
