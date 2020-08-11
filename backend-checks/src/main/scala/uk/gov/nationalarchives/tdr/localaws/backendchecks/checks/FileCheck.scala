package uk.gov.nationalarchives.tdr.localaws.backendchecks.checks

import java.util.UUID

import scala.concurrent.{ExecutionContext, Future}

trait FileCheck {
  def name: String

  def checkFile(fileId: UUID)(implicit executionContext: ExecutionContext): Future[Any]
}
