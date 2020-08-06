package uk.gov.nationalarchives.tdr.localaws.backendchecks.checks

import java.util.UUID

import uk.gov.nationalarchives.tdr.localaws.backendchecks.api.FileService
import uk.gov.nationalarchives.tdr.localaws.backendchecks.auth.TokenService
import uk.gov.nationalarchives.tdr.localaws.backendchecks.checks.metadata.FakeChecksum

import scala.concurrent.{ExecutionContext, Future}

class ChecksumCheck(
                     tokenService: TokenService,
                     fileService: FileService
                   )(implicit val executionContext: ExecutionContext) extends FileCheck {

  override def name: String = "checksum"

  override def checkFile(fileId: UUID)(implicit executionContext: ExecutionContext): Future[Any] = {
    for {
      token <- tokenService.token
      originalPath <- fileService.originalFileName(fileId, token)
      checksum = FakeChecksum.generate(originalPath)
      result <- fileService.saveChecksum(checksum, fileId, token)
    } yield result
  }
}
