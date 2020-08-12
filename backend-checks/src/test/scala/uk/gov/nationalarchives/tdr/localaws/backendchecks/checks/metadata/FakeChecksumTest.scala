package uk.gov.nationalarchives.tdr.localaws.backendchecks.checks.metadata

import java.nio.file.Paths

import org.scalatest.matchers.should
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatest.propspec.AnyPropSpec

class FakeChecksumTest extends AnyPropSpec with TableDrivenPropertyChecks with should.Matchers {

  case class ChecksumExample(fileName: String, expectedChecksum: String)
  val examples = Table(
    "Checksum metadata examples",
    ChecksumExample("some file name.txt", "fake-checksum"),
    ChecksumExample("another-file-name", "fake-checksum"),
    ChecksumExample("not-a-test-checksum-abc.pdf", "fake-checksum"),
    ChecksumExample("test-checksum", "fake-checksum"),
    ChecksumExample("test-checksum-abcd", "abcd"),
    ChecksumExample("test-checksum-EFGH.pdf", "EFGH"),
    ChecksumExample(
      "test-checksum-52b4ca01189d4e25b6f74010492f43b4be2e27c3c54286a2aed20c856b70f954.docx",
      "52b4ca01189d4e25b6f74010492f43b4be2e27c3c54286a2aed20c856b70f954"
    ),
  )

  property("the generated checksum is based on the file name") {
    forAll(examples) { example =>
      FakeChecksum.generate(Paths.get(example.fileName)) should equal(example.expectedChecksum)
    }
  }
}
