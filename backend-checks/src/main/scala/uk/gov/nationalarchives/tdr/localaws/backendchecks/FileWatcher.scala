package uk.gov.nationalarchives.tdr.localaws.backendchecks

import java.nio.file.StandardWatchEventKinds.ENTRY_CREATE
import java.nio.file._
import java.nio.file.attribute.BasicFileAttributes

import uk.gov.nationalarchives.tdr.localaws.backendchecks.checks.FileCheck

import scala.collection.mutable
import scala.concurrent.ExecutionContext
import scala.jdk.CollectionConverters._
import scala.util.{Failure, Success}

class FileWatcher(parentDirectory: Path, fileChecks: Seq[FileCheck])(implicit executionContext: ExecutionContext) {

  private val watcher: WatchService = FileSystems.getDefault.newWatchService
  private val initialPaths: Map[WatchKey, Path] = registerAll(parentDirectory)

  def startWatching: Unit = monitorChanges(initialPaths)

  private def registerAll(start: Path): Map[WatchKey, Path] = {
    val pathsByKey = mutable.Map.empty[WatchKey, Path]

    Files.walkFileTree(start, new SimpleFileVisitor[Path] {
      override def preVisitDirectory(directory: Path, attrs: BasicFileAttributes): FileVisitResult = {
        registerDirectory(directory, pathsByKey)
        FileVisitResult.CONTINUE
      }
    })

    pathsByKey.toMap
  }

  private def registerDirectory(directory: Path, pathsByKey: mutable.Map[WatchKey, Path]): Unit = {
    println(s"Registering directory $directory")

    val watchKey = directory.register(watcher, ENTRY_CREATE)
    pathsByKey(watchKey) = directory
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
        checkFilesInDirectory(fullPath)
        currentPaths ++ newPaths
      } else {
        runChecks(fullPath)
        currentPaths
      }
    }).toMap

    watchKey.reset()

    monitorChanges(updatedPaths)
  }

  def checkFilesInDirectory(directory: Path): Unit = {
    Files.walkFileTree(directory, new SimpleFileVisitor[Path] {
      override def visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult = {
        runChecks(file)
        FileVisitResult.CONTINUE
      }
    })
  }

  def runChecks(filePath: Path): () = {
    LocalFilePaths.extractFileId(filePath) match {
      case Success(fileId) => {
        // Log any errors returned by the file check, then ignore them to allow the watcher to keep running
        fileChecks.foreach(fileCheck => {
          fileCheck.checkFile(fileId).recover(error => {
            println(s"Error running ${fileCheck.name} check for file with path '$filePath' and ID '$fileId'", error)
          })
        })
      }
      case Failure(e) =>
        println(s"Error extracting file ID from path '$filePath', so skipping file check", e)
    }
  }
}
