package uk.gov.nationalarchives.tdr.localaws.backendchecks.checks

import java.util.UUID

trait FileCheck {
  def check(fileId: UUID): Unit
}
