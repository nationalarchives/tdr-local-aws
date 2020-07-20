package uk.gov.nationalarchives.tdr.localaws.backendchecks.checks

import java.util.UUID

import graphql.codegen.GetOriginalPath.getOriginalPath
import graphql.codegen.AddAntivirusMetadata.AddAntivirusMetadata
import graphql.codegen.types.AddAntivirusMetadataInput
import uk.gov.nationalarchives.tdr.GraphQLClient
import uk.gov.nationalarchives.tdr.localaws.backendchecks.auth.TokenService

import scala.concurrent.{ExecutionContext, Future}

class AntivirusCheck(
                        tokenService: TokenService,
                        getDocumentClient: GraphQLClient[getOriginalPath.Data, getOriginalPath.Variables],
                        antivirusClient: GraphQLClient[AddAntivirusMetadata.Data, AddAntivirusMetadata.Variables]
                      )(implicit val executionContext: ExecutionContext) extends FileCheck {

  private val eicarPattern = "($eicar)".r
  private val virusPattern = "(test-virus)".r

  def check(fileId: UUID): Future[Any] = {
    tokenService.token.flatMap(token => {
      val queryVariables = getOriginalPath.Variables(fileId)

      getDocumentClient.getResult(token, getOriginalPath.document, Some(queryVariables)).flatMap(data => {
        val originalPath = data.data match {
          case Some(metadata) => metadata.getClientFileMetadata.originalPath.get
          // TODO: Include original errors
          case None => throw new RuntimeException("Error in GraphQL response")
        }

        val result = originalPath match {
          case eicarPattern(_) => "SUSP_Just_EICAR"
          case virusPattern(_) => "test_virus"
          case _ => ""
        }

        val antivirusSoftware = "fake-local-antivirus"
        val fakeVersion = "1.0"
        val fakeDate = 123l
        val input = AddAntivirusMetadataInput(
          fileId,
          Some(antivirusSoftware),
          Some(result),
          Some(fakeVersion),
          Some(fakeVersion),
          Some(result),
          fakeDate
        )
        val mutationVariables = AddAntivirusMetadata.Variables(input)
        antivirusClient.getResult(token, AddAntivirusMetadata.document, Some(mutationVariables))
      })
    })
  }
}
