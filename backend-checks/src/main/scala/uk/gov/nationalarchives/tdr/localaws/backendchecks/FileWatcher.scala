package uk.gov.nationalarchives.tdr.localaws.backendchecks

import java.nio.file.StandardWatchEventKinds.ENTRY_CREATE
import java.nio.file._
import java.nio.file.attribute.BasicFileAttributes
import java.util.UUID

import uk.gov.nationalarchives.tdr.localaws.backendchecks.checks.FileCheck

import scala.collection.mutable
import scala.concurrent.ExecutionContext
import scala.jdk.CollectionConverters._
import scala.util.{Failure, Success, Try}

class FileWatcher(parentDirectory: Path, fileCheck: FileCheck)(implicit executionContext: ExecutionContext) {

  private val UuidLength = 36

  private val watcher = FileSystems.getDefault.newWatchService
  private val initialPaths = registerAll(parentDirectory)

  def watchDirectory: Unit = monitorChanges(initialPaths)

  private def registerAll(start: Path): Map[WatchKey, Path] = {
    val pathsByKey = mutable.Map.empty[WatchKey, Path]

    Files.walkFileTree(start, new SimpleFileVisitor[Path] {
      override def preVisitDirectory(directory: Path, attrs: BasicFileAttributes): FileVisitResult = {
        println(s"Registering directory $directory")

        val watchKey = directory.register(watcher, ENTRY_CREATE)
        pathsByKey(watchKey) = directory

        FileVisitResult.CONTINUE
      }
    })

    pathsByKey.toMap
  }

  @scala.annotation.tailrec
  private def monitorChanges(currentPaths: Map[WatchKey, Path]): Map[WatchKey, Path] = {
    println("Watching for changes")

    val watchKey: WatchKey = watcher.take()
    val eventPath = currentPaths(watchKey)

    val updatedPaths = watchKey.pollEvents().asScala.flatMap((event: WatchEvent[_]) => {
      val pathEvent: WatchEvent[Path] = event.asInstanceOf[WatchEvent[Path]]
      val newFileName = pathEvent.context()
      val fullPath = eventPath.resolve(newFileName)

      if (Files.isDirectory(fullPath)) {
        val newPaths = registerAll(fullPath)

        Files.walkFileTree(fullPath, new SimpleFileVisitor[Path] {
          override def visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult = {
            runFileChecks(file)

            FileVisitResult.CONTINUE
          }
        })

        currentPaths ++ newPaths
      } else {
        runFileChecks(fullPath)

        currentPaths
      }
    }).toMap

    watchKey.reset()

    monitorChanges(updatedPaths)
  }

  private def runFileChecks(path: Path): Unit = {
    extractFileId(path) match {
      case Success(fileId) => {
        // Log any errors returned by the file check, then ignore them to allow the watcher to keep running
        fileCheck.check(fileId).recover(error => {
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
