package uk.gov.nationalarchives.tdr.localaws.backendchecks.checks

import java.util.UUID

import scala.concurrent.Future

trait FileCheck {
  def check(fileId: UUID): Future[Any]
}
