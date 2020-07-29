package uk.gov.nationalarchives.tdr.localaws.backendchecks.checks

import java.nio.file.{Path, Paths}
import java.util.UUID

import graphql.codegen.AddFileMetadata.addFileMetadata
import graphql.codegen.GetOriginalPath.getOriginalPath
import graphql.codegen.types.AddFileMetadataInput
import uk.gov.nationalarchives.tdr.GraphQLClient
import uk.gov.nationalarchives.tdr.localaws.backendchecks.api.GraphQl.sendGraphQlRequest
import uk.gov.nationalarchives.tdr.localaws.backendchecks.auth.TokenService

import scala.concurrent.{ExecutionContext, Future}

class ChecksumCheck(
                     tokenService: TokenService,
                     getDocumentClient: GraphQLClient[getOriginalPath.Data, getOriginalPath.Variables],
                     addMetadataClient: GraphQLClient[addFileMetadata.Data, addFileMetadata.Variables]
                   )(implicit val executionContext: ExecutionContext) extends FileCheck {

  private val customChecksumPattern = "test-checksum-(\\w*)(?:\\.\\w+)".r
  private val defaultChecksum = "fake-checksum"

  override def checkName: String = "checksum"

  override def checkFileId(fileId: UUID): Future[Any] = {
    tokenService.token.flatMap(token => {
      val queryVariables = getOriginalPath.Variables(fileId)

      sendGraphQlRequest(getDocumentClient, token, queryVariables, getOriginalPath.document).map(result => {
        val originalPath = Paths.get(result.getClientFileMetadata.originalPath.get)

        val checksum = generateChecksum(originalPath)
        val addMetadataInput = AddFileMetadataInput("SHA256ServerSideChecksum", fileId, checksum)
        val mutationVariables = addFileMetadata.Variables(addMetadataInput)

        sendGraphQlRequest(addMetadataClient, token, mutationVariables, addFileMetadata.document)
      })
    })
  }

  private def generateChecksum(originalPath: Path): String = {
    originalPath.getFileName.toString match {
      case customChecksumPattern(customChecksum) => customChecksum
      case _ => defaultChecksum
    }
  }
}
