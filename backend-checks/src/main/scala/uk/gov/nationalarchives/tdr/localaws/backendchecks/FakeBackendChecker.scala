package uk.gov.nationalarchives.tdr.localaws.backendchecks

import java.nio.file.StandardWatchEventKinds.ENTRY_CREATE
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file._

import scala.jdk.CollectionConverters._

object FakeBackendChecker extends App {

  val parentDirectory = Paths.get("/tmp/test-data")
  val watcher = FileSystems.getDefault.newWatchService

  registerAll(parentDirectory)

  while(true) {
    println("Watching for changes")

    val watchKey: WatchKey = watcher.take()
    watchKey.pollEvents().asScala.foreach((event: WatchEvent[_]) => {
      val pathEvent: WatchEvent[Path] = event.asInstanceOf[WatchEvent[Path]]
      val newFileName = pathEvent.context()
      val fullPath = parentDirectory.resolve(newFileName)

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

  def registerAll(start: Path): Unit = {
    Files.walkFileTree(start, new SimpleFileVisitor[Path] {
      override def preVisitDirectory(directory: Path, attrs: BasicFileAttributes): FileVisitResult = {
        println(s"Registering directory $directory")

        directory.register(watcher, ENTRY_CREATE)

        FileVisitResult.CONTINUE
      }
    })
  }
}
