package uk.gov.nationalarchives.tdr.localaws.backendchecks.checks

import java.nio.file.Path
import java.util.UUID

import graphql.codegen.GetClientFileMetadata.getClientFileMetadata
import uk.gov.nationalarchives.tdr.GraphQLClient
import uk.gov.nationalarchives.tdr.localaws.backendchecks.auth.TokenService

import scala.concurrent.ExecutionContext

class AntivirusCheck(
                        tokenService: TokenService,
                        getDocumentClient: GraphQLClient[getClientFileMetadata.Data, getClientFileMetadata.Variables]
                      )(implicit val executionContext: ExecutionContext) extends FileCheck {
  // TODO: Pass fileId rather than path
  // TODO: Return Future
  def check(path: Path): Unit = {
    println(s"Getting file with path $path")

    val fileId = UUID.fromString(path.getFileName.toString)

    tokenService.token.map(token => {
      println("Got token: ")
      println(token)

      val queryVariables = getClientFileMetadata.Variables(fileId)

      getDocumentClient.getResult(token, getClientFileMetadata.document, Some(queryVariables)).map(response => {
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
