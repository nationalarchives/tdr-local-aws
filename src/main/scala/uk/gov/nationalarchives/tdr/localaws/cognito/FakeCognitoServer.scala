package uk.gov.nationalarchives.tdr.localaws.cognito

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.Materializer

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

object FakeCognitoServer extends App {

  val PORT = 4600

  implicit val actorSystem: ActorSystem = ActorSystem("graphql-server")
  implicit val materializer: Materializer = Materializer(actorSystem)

  scala.sys.addShutdownHook(() -> shutdown())

  Http().bindAndHandle(Routes.route, "0.0.0.0", PORT)

  def shutdown(): Unit = {
    actorSystem.terminate()
    Await.result(actorSystem.whenTerminated, 30 seconds)
  }
}
