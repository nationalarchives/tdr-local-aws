package uk.gov.nationalarchives.tdr.localaws.backendchecks.checks

import java.util.UUID

import graphql.codegen.GetClientFileMetadata.getClientFileMetadata
import graphql.codegen.AddAntivirusMetadata.AddAntivirusMetadata
import graphql.codegen.types.AddAntivirusMetadataInput
import uk.gov.nationalarchives.tdr.GraphQLClient
import uk.gov.nationalarchives.tdr.localaws.backendchecks.auth.TokenService

import scala.concurrent.{ExecutionContext, Future}

class AntivirusCheck(
                        tokenService: TokenService,
                        getDocumentClient: GraphQLClient[getClientFileMetadata.Data, getClientFileMetadata.Variables],
                        antivirusClient: GraphQLClient[AddAntivirusMetadata.Data, AddAntivirusMetadata.Variables]
                      )(implicit val executionContext: ExecutionContext) extends FileCheck {

  def check(fileId: UUID): Future[Any] = {
    tokenService.token.flatMap(token => {
      val queryVariables = getClientFileMetadata.Variables(fileId)

      getDocumentClient.getResult(token, getClientFileMetadata.document, Some(queryVariables)).flatMap(data => {
        val originalPath = data.data match {
          case Some(metadata) => metadata.getClientFileMetadata.originalPath.get
          // TODO: Include original errors
          case None => throw new RuntimeException("Error in GraphQL response")
        }

        val antivirusSoftware = "fake-local-antivirus"
        val result = "fake-antivirus-result"
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
