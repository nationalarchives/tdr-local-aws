package uk.gov.nationalarchives.tdr.localaws.backendchecks

import java.nio.file.StandardWatchEventKinds.ENTRY_CREATE
import java.nio.file._
import java.nio.file.attribute.BasicFileAttributes

import scala.collection.mutable
import scala.jdk.CollectionConverters._

object FakeBackendChecker extends App {

  val parentDirectory = Paths.get("/tmp/test-data")
  val watcher = FileSystems.getDefault.newWatchService

  val initialPaths = registerAll(parentDirectory)

  while(true) {
    println("Watching for changes")

    val watchKey: WatchKey = watcher.take()
    val eventPath = initialPaths(watchKey)

    watchKey.pollEvents().asScala.foreach((event: WatchEvent[_]) => {
      val pathEvent: WatchEvent[Path] = event.asInstanceOf[WatchEvent[Path]]
      val newFileName = pathEvent.context()
      val fullPath = eventPath.resolve(newFileName)

      if (Files.isDirectory(fullPath)) {
        registerAll(fullPath)

        Files.walkFileTree(fullPath, new SimpleFileVisitor[Path] {
          override def visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult = {
            runFileChecks(file)

            FileVisitResult.CONTINUE
          }
        })
      } else {
        runFileChecks(fullPath)
      }
    })

    watchKey.reset()
  }

  def runFileChecks(path: Path): Unit = {
    println(s"Placeholder for file checks on path $path")
  }

  def registerAll(start: Path): Map[WatchKey, Path] = {
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
}
