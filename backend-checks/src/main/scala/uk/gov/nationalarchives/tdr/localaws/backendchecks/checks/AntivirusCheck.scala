package uk.gov.nationalarchives.tdr.localaws.backendchecks.checks

import java.util.UUID

import graphql.codegen.GetClientFileMetadata.getClientFileMetadata
import uk.gov.nationalarchives.tdr.GraphQLClient
import uk.gov.nationalarchives.tdr.localaws.backendchecks.auth.TokenService

import scala.concurrent.{ExecutionContext, Future}

class AntivirusCheck(
                        tokenService: TokenService,
                        getDocumentClient: GraphQLClient[getClientFileMetadata.Data, getClientFileMetadata.Variables]
                      )(implicit val executionContext: ExecutionContext) extends FileCheck {

  def check(fileId: UUID): Future[Any] = {
    tokenService.token.flatMap(token => {
      val queryVariables = getClientFileMetadata.Variables(fileId)

      getDocumentClient.getResult(token, getClientFileMetadata.document, Some(queryVariables))
    })
  }
}
