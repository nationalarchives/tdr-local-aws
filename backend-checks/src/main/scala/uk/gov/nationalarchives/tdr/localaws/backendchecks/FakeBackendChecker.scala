package uk.gov.nationalarchives.tdr.localaws.backendchecks

import java.nio.file.StandardWatchEventKinds.ENTRY_CREATE
import java.nio.file.{FileSystems, Files, Path, Paths, WatchEvent, WatchKey}

import scala.jdk.CollectionConverters._

object FakeBackendChecker extends App {

  val parentDirectory = Paths.get("/tmp/test-data")

  val watcher = FileSystems.getDefault.newWatchService

  parentDirectory.register(watcher, ENTRY_CREATE)

  while(true) {
    println("Watching for changes")

    val watchKey: WatchKey = watcher.take()
    watchKey.pollEvents().asScala.foreach((event: WatchEvent[_]) => {
      val pathEvent: WatchEvent[Path] = event.asInstanceOf[WatchEvent[Path]]
      val newFileName = pathEvent.context()
      val fullPath = parentDirectory.resolve(newFileName)
      println(s"Found new file or directory at path $fullPath")

      if (Files.isDirectory(fullPath)) {
        println(s"Registering directory $fullPath")
        // TODO: Test nested path created
        fullPath.register(watcher, ENTRY_CREATE)
      }
    })

    watchKey.reset()
  }
}
