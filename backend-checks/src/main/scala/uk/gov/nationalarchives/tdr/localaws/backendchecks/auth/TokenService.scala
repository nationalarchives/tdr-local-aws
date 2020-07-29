package uk.gov.nationalarchives.tdr.localaws.backendchecks.auth

import com.nimbusds.oauth2.sdk.token.BearerAccessToken
import com.typesafe.config.Config
import uk.gov.nationalarchives.tdr.keycloak.KeycloakUtils

import scala.concurrent.{ExecutionContext, Future}

class TokenService(config: Config)(implicit val executionContext: ExecutionContext) {

  private val keycloakUtils: KeycloakUtils = KeycloakUtils(config.getString("auth.baseUrl"))
  private val clientId = config.getString("auth.client.id")
  private val clientSecret = config.getString("auth.client.secret")

  def token: Future[BearerAccessToken] = keycloakUtils.serviceAccountToken(clientId, clientSecret)
}
