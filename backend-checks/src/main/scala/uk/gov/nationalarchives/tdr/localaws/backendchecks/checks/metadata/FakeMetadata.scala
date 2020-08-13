package uk.gov.nationalarchives.tdr.localaws.backendchecks.checks.metadata

import java.nio.file.Path

import graphql.codegen.types.FFIDMetadataInputMatches
import uk.gov.nationalarchives.tdr.localaws.backendchecks.api.{AntivirusMetadata, FileFormatMetadata}

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

object FakeFileFormat {

  private val noFileFormatPattern = "(test-fmt-none)(?:.*)".r
  private val customFileFormatPattern = "test-fmt-(\\d+)(?:.*)".r
  private val customFileExperimentalFormatPattern = "test-x-fmt-(\\d+)(?:.*)".r
  private val textFilePronomId = "x-fmt/111"

  def generate(originalFileName: Path): FileFormatMetadata = {
    val extension = originalFileName.toString.split("\\.") match {
      case parts if parts.size == 1 => None
      case parts => Some(parts.last)
    }

    val pronomId = originalFileName.toString match {
      case noFileFormatPattern(_) => None
      case customFileFormatPattern(pronomId) => Some(s"fmt/$pronomId")
      case customFileExperimentalFormatPattern(pronomId) => Some(s"x-fmt/$pronomId")
      case _ => Some(textFilePronomId)
    }

    val formatMatch = FFIDMetadataInputMatches(
      extension,
      "fake-file-format-identification-basis",
      pronomId
    )

    FileFormatMetadata(
      "fake-file-format-software",
      "fake-file-format-software-version",
      "fake-binary-signature-file-version",
      "fake-container-signature-file-version",
      "fake-file-format-match-method",
      List(formatMatch)
    )
  }
}
