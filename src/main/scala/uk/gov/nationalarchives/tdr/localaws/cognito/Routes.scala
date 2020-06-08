package uk.gov.nationalarchives.tdr.localaws.cognito

import akka.http.scaladsl.server.Directives.complete
import akka.http.scaladsl.server.Route
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._

object Routes {

  // Fake access key and secret key required to upload files to a local S3Ninja container. These values are fixed in the
  // S3Ninja config: https://github.com/scireum/s3ninja/blob/master/src/main/resources/application.conf
  //
  // Ideally, they would be injected into this application and S3Ninja as environment variables, but there's no obvious
  // way to set them in S3Ninja without building our own Docker image and modifying the contents of
  // /home/sirius/app/application.conf. These values seem to be stable (they haven't been changed in 3 years) and are
  // only used in a development environment, so committing them is not a security risk.
  val localAccessKeyId = "AKIAIOSFODNN7EXAMPLE"
  val localSecretKey = "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY"

  val route: Route = cors() {
    complete(
      CognitoResponse("some-fake-identity-id", Credentials(localAccessKeyId, localSecretKey))
    )
  }
}

case class CognitoResponse(IdentityId: String, Credentials: Credentials)
case class Credentials(AccessKeyId: String, SecretKey: String)
