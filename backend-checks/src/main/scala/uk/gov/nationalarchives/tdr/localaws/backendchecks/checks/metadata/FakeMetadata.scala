package uk.gov.nationalarchives.tdr.localaws.backendchecks.checks.metadata

import java.nio.file.Path

import uk.gov.nationalarchives.tdr.localaws.backendchecks.api.AntivirusMetadata

object FakeAntivirusMetadata {

  private val eicarPattern = "(eicar).*".r
  private val virusPattern = "(test-virus).*".r

  def generate(originalFileName: Path): AntivirusMetadata = {
    val result = originalFileName.toString match {
      case eicarPattern(_) => "SUSP_Just_EICAR"
      case virusPattern(_) => "test_virus"
      case _ => ""
    }

    val antivirusSoftware = "fake-local-antivirus"
    val fakeVersion = "1.0"

    AntivirusMetadata(antivirusSoftware, result, fakeVersion)
  }
}

object FakeChecksum {

  private val customChecksumPattern = "test-checksum-(\\w*)(?:\\.\\w+)".r
  private val defaultChecksum = "fake-checksum"

  def generate(originalFileName: Path): String = {
    originalFileName.toString match {
      case customChecksumPattern(customChecksum) => customChecksum
      case _ => defaultChecksum
    }
  }
}
