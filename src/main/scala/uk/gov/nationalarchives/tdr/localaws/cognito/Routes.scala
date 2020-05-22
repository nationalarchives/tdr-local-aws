package uk.gov.nationalarchives.tdr.localaws.cognito

import akka.http.scaladsl.server.Directives.complete
import akka.http.scaladsl.server.Route
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._

object Routes {
  val route: Route = cors() {
    complete("{\"IdentityId\": \"some-fake-identity-id\", \"Credentials\": {}}")
  }
}
