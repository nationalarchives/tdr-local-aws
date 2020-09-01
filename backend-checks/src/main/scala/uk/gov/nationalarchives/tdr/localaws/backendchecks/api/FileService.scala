package uk.gov.nationalarchives.tdr.localaws.backendchecks.api

import java.nio.file.{Path, Paths}
import java.time.Instant
import java.util.UUID

import com.nimbusds.oauth2.sdk.token.BearerAccessToken
import graphql.codegen.AddAntivirusMetadata.AddAntivirusMetadata
import graphql.codegen.AddFileMetadata.addFileMetadata
import graphql.codegen.GetOriginalPath.getOriginalPath
import graphql.codegen.AddFFIDMetadata.addFFIDMetadata
import graphql.codegen.types.{AddAntivirusMetadataInput, AddFileMetadataInput, FFIDMetadataInput, FFIDMetadataInputMatches}
import uk.gov.nationalarchives.tdr.GraphQLClient
import uk.gov.nationalarchives.tdr.localaws.backendchecks.api.GraphQl.sendGraphQlRequest

import scala.concurrent.{ExecutionContext, Future}

class FileService(
                   getDocumentClient: GraphQLClient[getOriginalPath.Data, getOriginalPath.Variables],
                   antivirusClient: GraphQLClient[AddAntivirusMetadata.Data, AddAntivirusMetadata.Variables],
                   addMetadataClient: GraphQLClient[addFileMetadata.Data, addFileMetadata.Variables],
                   addFileFormatClient: GraphQLClient[addFFIDMetadata.Data, addFFIDMetadata.Variables]
                 ) {

  def originalFileName(fileId: UUID, token: BearerAccessToken)(implicit executionContext: ExecutionContext): Future[Path] = {
    val queryVariables = getOriginalPath.Variables(fileId)

    sendGraphQlRequest(getDocumentClient, token, queryVariables, getOriginalPath.document).map(result => {
      Paths.get(result.getClientFileMetadata.originalPath.get).getFileName
    })
  }

  def saveAntivirusResult(
                           metadata: AntivirusMetadata,
                           fileId: UUID,
                           token: BearerAccessToken
                         )(implicit executionContext: ExecutionContext): Future[AddAntivirusMetadata.Data] = {
    val mutationInput = AddAntivirusMetadataInput(
      fileId,
      metadata.software,
      metadata.version,
      metadata.version,
      metadata.result,
      Instant.now().toEpochMilli
    )
    val mutationVariables = AddAntivirusMetadata.Variables(mutationInput)

    sendGraphQlRequest(antivirusClient, token, mutationVariables, AddAntivirusMetadata.document)
  }

  def saveChecksum(
                    checksum: String,
                    fileId: UUID,
                    token: BearerAccessToken
                  )(implicit executionContext: ExecutionContext): Future[addFileMetadata.Data] = {
    val addMetadataInput = AddFileMetadataInput("SHA256ServerSideChecksum", fileId, checksum)
    val mutationVariables = addFileMetadata.Variables(addMetadataInput)

    sendGraphQlRequest(addMetadataClient, token, mutationVariables, addFileMetadata.document)
  }

  def saveFileFormatResult(
                            metadata: FileFormatMetadata,
                            fileId: UUID, token: BearerAccessToken
                          )(implicit executionContext: ExecutionContext): Future[addFFIDMetadata.Data] = {
    val addMetadataInput = FFIDMetadataInput(
      fileId,
      metadata.software,
      metadata.softwareVersion,
      metadata.binarySignatureFileVersion,
      metadata.containerSignatureFileVersion,
      metadata.matchMethod,
      metadata.matches
    )
    val mutationVariables = addFFIDMetadata.Variables(addMetadataInput)

    sendGraphQlRequest(addFileFormatClient, token, mutationVariables, addFFIDMetadata.document)
  }
}

case class AntivirusMetadata(software: String, result: String, version: String)
case class FileFormatMetadata(
                               software: String,
                               softwareVersion: String,
                               binarySignatureFileVersion: String,
                               containerSignatureFileVersion: String,
                               matchMethod: String,
                               matches: List[FFIDMetadataInputMatches]
                             )
