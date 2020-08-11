package uk.gov.nationalarchives.tdr.localaws.backendchecks.checks.metadata

import java.nio.file.Paths

import org.scalatest.matchers.should
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatest.propspec.AnyPropSpec

class FakeAntivirusMetadataTest extends AnyPropSpec with TableDrivenPropertyChecks with should.Matchers {

  case class AntivirusExample(fileName: String, expectedAntivirusResult: String)
  val examples = Table(
    "Antivirus metadata examples",
    AntivirusExample("some file name.txt", ""),
    AntivirusExample("something-eicar", ""),
    AntivirusExample("eicar", "SUSP_Just_EICAR"),
    AntivirusExample("eicar.txt", "SUSP_Just_EICAR"),
    AntivirusExample("eicar-some-suffix.exe", "SUSP_Just_EICAR"),
    AntivirusExample("test-virus", "test_virus"),
    AntivirusExample("test-virus.jpg", "test_virus"),
    AntivirusExample("test-virus-some-suffix.exe", "test_virus"),
  )

  property("the antivirus result is based on the file name") {
    forAll(examples) { example =>
      FakeAntivirusMetadata.generate(Paths.get(example.fileName)).result should equal(example.expectedAntivirusResult)
    }
  }
}
