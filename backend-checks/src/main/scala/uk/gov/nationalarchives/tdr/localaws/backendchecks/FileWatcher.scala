package uk.gov.nationalarchives.tdr.localaws.backendchecks

import java.nio.file.StandardWatchEventKinds.ENTRY_CREATE
import java.nio.file._
import java.nio.file.attribute.BasicFileAttributes

import uk.gov.nationalarchives.tdr.localaws.backendchecks.checks.FileCheck

import scala.collection.mutable
import scala.concurrent.ExecutionContext
import scala.jdk.CollectionConverters._

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
        fileChecks.foreach(_.checkPath(fullPath))
        currentPaths
      }
    }).toMap

    watchKey.reset()

    monitorChanges(updatedPaths)
  }

  def checkFilesInDirectory(directory: Path): Unit = {
    Files.walkFileTree(directory, new SimpleFileVisitor[Path] {
      override def visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult = {
        fileChecks.foreach(_.checkPath(file))

        FileVisitResult.CONTINUE
      }
    })
  }
}
