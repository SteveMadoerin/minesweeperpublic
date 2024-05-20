package de.htwg.sa.minesweeper.persistence

import de.htwg.sa.minesweeper.persistence.entity.Game
import de.htwg.sa.minesweeper.persistence.persistenceComponent.PersistenceApi
import de.htwg.sa.minesweeper.persistence.persistenceComponent.config.Default.given
import de.htwg.sa.minesweeper.persistence.persistenceComponent.persistenceSlickImpl.MongoDB

import scala.io.StdIn.readLine

object PersistenceService {
  def main(args: Array[String]): Unit = {
    PersistenceApi().start() // on Port 9083
    val mongoInstance = new MongoDB()
    val game = Game(6, 5, 0, "Playing")
    mongoInstance.insertGame(game)
    val game2 = mongoInstance.loadGame(5)
    println(game2.get.side)
    println("Press RETURN to stop...")
    readLine() // Keep the application alive until user presses return
  }
}
