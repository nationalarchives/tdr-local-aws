package uk.gov.nationalarchives.tdr.localaws.backendchecks.auth

import com.nimbusds.oauth2.sdk.token.BearerAccessToken
import com.typesafe.config.Config
import sttp.client.SttpBackend
import sttp.client.asynchttpclient.WebSocketHandler
import sttp.client.asynchttpclient.future.AsyncHttpClientFutureBackend
import uk.gov.nationalarchives.tdr.keycloak.KeycloakUtils

import scala.concurrent.{ExecutionContext, Future}

class TokenService(config: Config)(implicit val executionContext: ExecutionContext) {

  implicit private val backend: SttpBackend[Future, Nothing, WebSocketHandler] = AsyncHttpClientFutureBackend()

  private val keycloakUtils: KeycloakUtils = KeycloakUtils(config.getString("auth.baseUrl"))
  private val clientId = config.getString("auth.client.id")
  private val clientSecret = config.getString("auth.client.secret")

  def token: Future[BearerAccessToken] = keycloakUtils.serviceAccountToken(clientId, clientSecret)
}
