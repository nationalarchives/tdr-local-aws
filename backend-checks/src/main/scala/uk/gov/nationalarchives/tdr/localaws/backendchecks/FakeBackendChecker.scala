package uk.gov.nationalarchives.tdr.localaws.backendchecks

import java.nio.file.StandardWatchEventKinds.ENTRY_CREATE
import java.nio.file._
import java.nio.file.attribute.BasicFileAttributes

import com.typesafe.config.{Config, ConfigFactory}
import uk.gov.nationalarchives.tdr.localaws.backendchecks.checks.AntivirusChecker

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.jdk.CollectionConverters._

object FakeBackendChecker extends App {

  val config: Config = ConfigFactory.load
  val antivirusChecker = new AntivirusChecker(config)

  val parentDirectory = Paths.get("/tmp/test-data")
  val watcher = FileSystems.getDefault.newWatchService

  val initialPaths = registerAll(parentDirectory)

  monitorChanges(initialPaths)

  def runFileChecks(path: Path): Unit = {
    antivirusChecker.check(path)
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

  @scala.annotation.tailrec
  def monitorChanges(currentPaths: Map[WatchKey, Path]): Map[WatchKey, Path] = {
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
}
