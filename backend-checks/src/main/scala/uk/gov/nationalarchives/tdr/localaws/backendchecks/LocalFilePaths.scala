package uk.gov.nationalarchives.tdr.localaws.backendchecks

import java.nio.file.Path
import java.util.UUID

import scala.util.{Failure, Try}

object LocalFilePaths {

  private val UuidLength = 36

  def extractFileId(path: Path): Try[UUID] = {
    val fileName = path.getFileName.toString
    if (fileName.length < UuidLength) {
      return Failure(new IllegalArgumentException(s"Path '$path' is too short to contain a valid UUID"))
    }

    // S3Ninja does not store S3 folders as filesystem folders. Instead it puts underscores between the folder
    // name and the filename, so we have to extract the last 36 digits of the filename to get the filename
    // that would have been saved to S3.
    Try(UUID.fromString(path.getFileName.toString.takeRight(UuidLength)))
  }
}
