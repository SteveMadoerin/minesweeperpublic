package de.htwg.sa.minesweeper.persistence

import de.htwg.sa.minesweeper.persistence.entity.Game
import de.htwg.sa.minesweeper.persistence.persistenceComponent.config.Default.given
import de.htwg.sa.minesweeper.persistence.persistenceComponent.persistenceMongoImpl.Dao.GameDao
import de.htwg.sa.minesweeper.persistence.persistenceComponent.persistenceMongoImpl.Persistence
import de.htwg.sa.minesweeper.persistence.persistenceComponent.persistenceSlickImpl.PersistenceApi

import scala.io.StdIn.readLine

object PersistenceService {
  def main(args: Array[String]): Unit = {
    PersistenceApi().start() // on Port 9083
    val mongoInstance = new Persistence()
    val game = Game(6, 5, 0, "Playing") // id = 13
    val game3 = Game(10, 9, 0, "Playing") // id = 14
    val gameDao = new GameDao(mongoInstance.db)
    val resultGame = gameDao.findById(5)
    println(resultGame.side) // 77
    gameDao.update(1, game3)




    //mongoInstance.insertGame(game)
    //val game2 = mongoInstance.loadGame(5)
    //println(game2.get.side)
    println("Press RETURN to stop...")
    readLine() // Keep the application alive until user presses return
  }
}
