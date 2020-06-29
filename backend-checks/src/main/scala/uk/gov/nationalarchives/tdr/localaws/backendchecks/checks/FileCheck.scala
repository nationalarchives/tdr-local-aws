package uk.gov.nationalarchives.tdr.localaws.backendchecks.checks

import java.nio.file.Path

trait FileCheck {
  def check(path: Path): Unit
}
