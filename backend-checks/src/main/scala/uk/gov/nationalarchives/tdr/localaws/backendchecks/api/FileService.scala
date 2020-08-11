package uk.gov.nationalarchives.tdr.localaws.backendchecks.api

import java.nio.file.{Path, Paths}
import java.time.Instant
import java.util.UUID

import com.nimbusds.oauth2.sdk.token.BearerAccessToken
import graphql.codegen.AddAntivirusMetadata.AddAntivirusMetadata
import graphql.codegen.AddFileMetadata.addFileMetadata
import graphql.codegen.GetOriginalPath.getOriginalPath
import graphql.codegen.types.{AddAntivirusMetadataInput, AddFileMetadataInput}
import uk.gov.nationalarchives.tdr.GraphQLClient
import uk.gov.nationalarchives.tdr.localaws.backendchecks.api.GraphQl.sendGraphQlRequest

import scala.concurrent.{ExecutionContext, Future}

class FileService(
                   getDocumentClient: GraphQLClient[getOriginalPath.Data, getOriginalPath.Variables],
                   antivirusClient: GraphQLClient[AddAntivirusMetadata.Data, AddAntivirusMetadata.Variables],
                   addMetadataClient: GraphQLClient[addFileMetadata.Data, addFileMetadata.Variables]
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
      Some(metadata.software),
      Some(metadata.result),
      Some(metadata.version),
      Some(metadata.version),
      Some(metadata.result),
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
}

case class AntivirusMetadata(software: String, result: String, version: String)
