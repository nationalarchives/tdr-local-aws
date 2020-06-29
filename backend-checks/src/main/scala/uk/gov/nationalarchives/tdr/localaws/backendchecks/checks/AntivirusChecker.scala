package uk.gov.nationalarchives.tdr.localaws.backendchecks.checks

import java.nio.file.Path

class AntivirusChecker {
  def check(path: Path): Unit = {
    println(s"Placeholder for antivirus checks on path $path")
  }
}
