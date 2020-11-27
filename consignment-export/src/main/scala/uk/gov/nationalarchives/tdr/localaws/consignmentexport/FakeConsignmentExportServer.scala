package uk.gov.nationalarchives.tdr.localaws.consignmentexport

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.Materializer
import com.typesafe.config.ConfigFactory

import scala.concurrent.{Await, ExecutionContextExecutor}
import scala.concurrent.duration._
import scala.language.postfixOps

object FakeConsignmentExportServer extends App {
  private val config = ConfigFactory.load
  private val port = config.getInt("httpServer.port")

  implicit val actorSystem: ActorSystem = ActorSystem("graphql-server")
  implicit val materialiser: Materializer = Materializer(actorSystem)
  implicit val executionContext: ExecutionContextExecutor = materialiser.executionContext

  private val routes = new Routes(config)

  scala.sys.addShutdownHook(() -> shutdown())

  Http().bindAndHandle(routes.route, "0.0.0.0", port)

  def shutdown(): Unit = {
    actorSystem.terminate()
    Await.result(actorSystem.whenTerminated, 30 seconds)
  }
}
