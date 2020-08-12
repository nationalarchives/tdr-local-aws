package uk.gov.nationalarchives.tdr.localaws.backendchecks.checks.metadata

import java.nio.file.Paths

import org.scalatest.matchers.should
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatest.propspec.AnyPropSpec

class FakeFileFormatTest extends AnyPropSpec with TableDrivenPropertyChecks with should.Matchers {

  case class FileFormatExample(
                                fileName: String,
                                expectedFileFormatId: Option[String],
                                expectedExtension: Option[String]
                              )
  val defaultFileFormat = "x-fmt/111"
  val examples = Table(
    "File format metadata examples",
    FileFormatExample("some file name", Some(defaultFileFormat), None),
    FileFormatExample("some file name.txt", Some(defaultFileFormat), Some("txt")),
    FileFormatExample("another-file.pdf", Some(defaultFileFormat), Some("pdf")),
    FileFormatExample("test-fmt", Some(defaultFileFormat), None),
    FileFormatExample("test-fmt-none", None, None),
    FileFormatExample("test-fmt-none.exe", None, Some("exe")),
    FileFormatExample("test-fmt-abc", Some(defaultFileFormat), None),
    FileFormatExample("test-fmt-123", Some("fmt/123"), None),
    FileFormatExample("test-fmt-456789.pdf", Some("fmt/456789"), Some("pdf")),
    FileFormatExample("test-x-fmt", Some(defaultFileFormat), None),
    FileFormatExample("test-x-fmt-def", Some(defaultFileFormat), None),
    FileFormatExample("test-x-fmt-012", Some("x-fmt/012"), None),
    FileFormatExample("test-x-fmt-3.csv", Some("x-fmt/3"), Some("csv")),
  )

  property("the file format result is based on the file name") {
    forAll(examples) { example =>
      val metadata = FakeFileFormat.generate(Paths.get(example.fileName))
      metadata.matches should have size 1
      metadata.matches(0).extension should equal(example.expectedExtension)
      metadata.matches(0).puid should equal(example.expectedFileFormatId)
    }
  }
}
