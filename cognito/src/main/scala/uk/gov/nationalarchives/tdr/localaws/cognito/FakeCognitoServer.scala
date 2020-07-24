package uk.gov.nationalarchives.tdr.localaws.cognito

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.Materializer
import com.typesafe.config.ConfigFactory

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

object FakeCognitoServer extends App {

  private val config = ConfigFactory.load
  private val port = config.getInt("httpServer.port")

  private val routes = new Routes(config)

  implicit val actorSystem: ActorSystem = ActorSystem("graphql-server")
  implicit val materializer: Materializer = Materializer(actorSystem)

  scala.sys.addShutdownHook(() -> shutdown())

  Http().bindAndHandle(routes.route, "0.0.0.0", port)

  def shutdown(): Unit = {
    actorSystem.terminate()
    Await.result(actorSystem.whenTerminated, 30 seconds)
  }
}
