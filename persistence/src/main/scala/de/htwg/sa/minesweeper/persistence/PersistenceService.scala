package de.htwg.sa.minesweeper.persistence

import de.htwg.sa.minesweeper.persistence.persistenceComponent.config.Default.given
import de.htwg.sa.minesweeper.persistence.persistenceComponent.persistenceSlickImpl.PersistenceApi

import scala.io.StdIn.readLine

object PersistenceService {
  def main(args: Array[String]): Unit = {
    PersistenceApi().start() // on Port 9083
    println("Press RETURN to stop...")
    readLine()
  }
}
