package uk.gov.nationalarchives.tdr.localaws.cognito

import akka.http.scaladsl.server.Directives.complete
import akka.http.scaladsl.server.Route
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._

object Routes {
  val route: Route = cors() {
    complete(CognitoResponse("some-fake-identity-id", Credentials()))
  }
}

case class CognitoResponse(IdentityId: String, Credentials: Credentials)
case class Credentials()
