package uk.gov.nationalarchives.tdr.localaws.backendchecks.checks

import java.nio.file.{Path, Paths}
import java.time.Instant
import java.util.UUID

import graphql.codegen.AddAntivirusMetadata.AddAntivirusMetadata
import graphql.codegen.GetOriginalPath.getOriginalPath
import graphql.codegen.types.AddAntivirusMetadataInput
import uk.gov.nationalarchives.tdr.GraphQLClient
import uk.gov.nationalarchives.tdr.localaws.backendchecks.api.GraphQl.sendGraphQlRequest
import uk.gov.nationalarchives.tdr.localaws.backendchecks.auth.TokenService

import scala.concurrent.{ExecutionContext, Future}

class AntivirusCheck(
                        tokenService: TokenService,
                        getDocumentClient: GraphQLClient[getOriginalPath.Data, getOriginalPath.Variables],
                        antivirusClient: GraphQLClient[AddAntivirusMetadata.Data, AddAntivirusMetadata.Variables]
                      )(implicit val executionContext: ExecutionContext) extends FileCheck {

  private val eicarPattern = "(eicar).*".r
  private val virusPattern = "(test-virus).*".r

  override def checkName: String = "antivirus"

  override def checkFileId(fileId: UUID): Future[Any] = {
    tokenService.token.flatMap(token => {
      val queryVariables = getOriginalPath.Variables(fileId)

      sendGraphQlRequest(getDocumentClient, token, queryVariables, getOriginalPath.document).map(result => {
        val originalPath = Paths.get(result.getClientFileMetadata.originalPath.get)

        val metadataMutationInput = antivirusMetadata(fileId, originalPath)
        val mutationVariables = AddAntivirusMetadata.Variables(metadataMutationInput)

        sendGraphQlRequest(antivirusClient, token, mutationVariables, AddAntivirusMetadata.document)
      })
    })
  }

  private def antivirusMetadata(fileId: UUID, originalPath: Path): AddAntivirusMetadataInput = {
    val result = originalPath.getFileName.toString match {
      case eicarPattern(_) => "SUSP_Just_EICAR"
      case virusPattern(_) => "test_virus"
      case _ => ""
    }

    val antivirusSoftware = "fake-local-antivirus"
    val fakeVersion = "1.0"
    val fakeDate = Instant.now()

    AddAntivirusMetadataInput(
      fileId,
      Some(antivirusSoftware),
      Some(result),
      Some(fakeVersion),
      Some(fakeVersion),
      Some(result),
      fakeDate.toEpochMilli
    )
  }
}
