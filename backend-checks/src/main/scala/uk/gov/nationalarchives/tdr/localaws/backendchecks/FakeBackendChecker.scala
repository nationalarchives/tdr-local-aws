package uk.gov.nationalarchives.tdr.localaws.backendchecks

import java.nio.file.StandardWatchEventKinds.ENTRY_CREATE
import java.nio.file.{FileSystems, Path, Paths, WatchEvent, WatchKey}

import scala.jdk.CollectionConverters._

object FakeBackendChecker extends App {

  val path = Paths.get("/tmp/test-data")

  val watcher = FileSystems.getDefault.newWatchService

  // TODO: Recursively register paths? Or monitor for new directories?
  path.register(watcher, ENTRY_CREATE)

  while(true) {
    println("In loop")
//    Thread.sleep(2000)

    val key: WatchKey = watcher.take()
    key.pollEvents().asScala.foreach((event: WatchEvent[_]) => {
      val pathEvent: WatchEvent[Path] = event.asInstanceOf[WatchEvent[Path]]
      val name = pathEvent.context()
      println(s"Found file with name $name")
    })
  }
}
