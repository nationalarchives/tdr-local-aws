package uk.gov.nationalarchives.tdr.localaws.backendchecks.checks

import java.util.UUID

import uk.gov.nationalarchives.tdr.localaws.backendchecks.api.FileService
import uk.gov.nationalarchives.tdr.localaws.backendchecks.auth.TokenService
import uk.gov.nationalarchives.tdr.localaws.backendchecks.checks.metadata.FakeAntivirusMetadata

import scala.concurrent.{ExecutionContext, Future}

class AntivirusCheck(
                      tokenService: TokenService,
                      fileService: FileService
                    )(implicit val executionContext: ExecutionContext) extends FileCheck {

  override def name: String = "antivirus"

  override def checkFile(fileId: UUID)(implicit executionContext: ExecutionContext): Future[Any] = {
    for {
      token <- tokenService.token
      originalPath <- fileService.originalFileName(fileId, token)
      metadata = FakeAntivirusMetadata.generate(originalPath)
      result <- fileService.saveAntivirusResult(metadata, fileId, token)
    } yield result
  }
}
