package uk.gov.nationalarchives.tdr.localaws.backendchecks.checks

import java.nio.file.Path
import java.util.UUID

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

trait FileCheck {

  private val UuidLength = 36

  def checkFileId(fileId: UUID): Future[Any]

  def checkPath(path: Path)(implicit executionContext: ExecutionContext): Unit = {
    extractFileId(path) match {
      case Success(fileId) => {
        // Log any errors returned by the file check, then ignore them to allow the watcher to keep running
        checkFileId(fileId).recover(error => {
          println(s"Error saving antivirus result for path '$path'", error)
        })
      }
      case Failure(e: IllegalArgumentException) =>
        println(s"Filename at path '$path' does not end in a UUID, so skipping file checks")
      case Failure(e) =>
        println(s"Error extracting file ID from path '$path'", e)
    }
  }

  private def extractFileId(path: Path): Try[UUID] = {
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
