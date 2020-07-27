package uk.gov.nationalarchives.tdr.localaws.backendchecks.checks

import java.util.UUID

import graphql.codegen.AddFileMetadata.addFileMetadata
import graphql.codegen.types.AddFileMetadataInput
import uk.gov.nationalarchives.tdr.GraphQLClient
import uk.gov.nationalarchives.tdr.localaws.backendchecks.api.GraphQl.sendGraphQlRequest
import uk.gov.nationalarchives.tdr.localaws.backendchecks.auth.TokenService

import scala.concurrent.{ExecutionContext, Future}

class ChecksumCheck(
                     tokenService: TokenService,
                     addMetadataClient: GraphQLClient[addFileMetadata.Data, addFileMetadata.Variables]
                   )(implicit val executionContext: ExecutionContext) extends FileCheck {

  override def checkName: String = "checksum"

  override def checkFileId(fileId: UUID): Future[Any] = {
    tokenService.token.flatMap(token => {
      val checksum = "fake-checksum"
      val addMetadataInput = AddFileMetadataInput("SHA256ServerSideChecksum", fileId, checksum)
      val mutationVariables = addFileMetadata.Variables(addMetadataInput)

      sendGraphQlRequest(addMetadataClient, token, mutationVariables, addFileMetadata.document)
    })
  }
}
