package uk.gov.nationalarchives.tdr.localaws.cognito

import akka.http.scaladsl.server.Directives.complete
import akka.http.scaladsl.server.Route
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._
import com.typesafe.config.Config
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._

class Routes(config: Config) {

  private val localAccessKeyId = config.getString("s3.accessKeyId")
  private val localSecretKey = config.getString("s3.secretKey")

  val route: Route = cors() {
    complete(
      CognitoResponse("some-fake-identity-id", Credentials(localAccessKeyId, localSecretKey))
    )
  }
}

case class CognitoResponse(IdentityId: String, Credentials: Credentials)
case class Credentials(AccessKeyId: String, SecretKey: String)
