package uk.gov.nationalarchives.tdr.localaws.cognito

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.complete
import akka.http.scaladsl.server.Route

object Routes {
  val route: Route = {
    complete(StatusCodes.OK)
  }
}
