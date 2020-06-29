package uk.gov.nationalarchives.tdr.localaws.backendchecks.checks

import java.nio.file.Path
import java.util.UUID

import com.typesafe.config.{Config, ConfigFactory}
import graphql.codegen.GetClientFileMetadata
import uk.gov.nationalarchives.tdr.GraphQLClient
import uk.gov.nationalarchives.tdr.keycloak.KeycloakUtils

import scala.concurrent.ExecutionContext

class AntivirusChecker(implicit val executionContext: ExecutionContext) {
  // TODO: Inject
  val config: Config = ConfigFactory.load

  // TODO: Move API URL to config
  private val apiUrl = "http://localhost:8080/graphql"
  private val getMetadataType = GetClientFileMetadata.getClientFileMetadata
  private val getDocumentClient = new GraphQLClient[getMetadataType.Data, getMetadataType.Variables](apiUrl)

  val keycloakUtils: KeycloakUtils = KeycloakUtils(config.getString("auth.baseUrl"))

  // TODO: Pass fileId rather than path
  // TODO: Return Future
  def check(path: Path): Unit = {
    println(s"Getting file with path $path")

    val fileId = UUID.fromString(path.getFileName.toString)

    val clientId = config.getString("auth.client.id")
    val clientSecret = config.getString("auth.client.secret")
    keycloakUtils.serviceAccountToken(clientId, clientSecret).map(token => {
      println("Got token: ")
      println(token)

      val queryVariables = getMetadataType.Variables(fileId)

      getDocumentClient.getResult(token, getMetadataType.document, Some(queryVariables)).map(response => {
        println("Response:")
        println(response.data)
        println(response.errors)
      }
      ).recover(e => {
        println("Error in API response")
        println(e)
      })
    })
  }
}
