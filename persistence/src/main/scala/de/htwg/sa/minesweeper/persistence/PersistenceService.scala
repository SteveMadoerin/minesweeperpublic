package de.htwg.sa.minesweeper.persistence

import de.htwg.sa.minesweeper.persistence.persistenceComponent.PersistenceApi
import de.htwg.sa.minesweeper.persistence.persistenceComponent.config.Default.given

import scala.io.StdIn.readLine

object PersistenceService {
  def main(args: Array[String]): Unit = {
    PersistenceApi().start() // on Port 9083
    println("Press RETURN to stop...")
    readLine() // Keep the application alive until user presses return
  }
}
