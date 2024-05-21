package de.htwg.sa.minesweeper.persistence

import de.htwg.sa.minesweeper.persistence.entity.{Field, Game}
import de.htwg.sa.minesweeper.persistence.persistenceComponent.config.Default.given
import de.htwg.sa.minesweeper.persistence.persistenceComponent.persistenceMongoImpl.Dao.{FieldDao, GameDao}
import de.htwg.sa.minesweeper.persistence.persistenceComponent.persistenceMongoImpl.Persistence
import de.htwg.sa.minesweeper.persistence.persistenceComponent.persistenceSlickImpl.PersistenceApi

import scala.io.StdIn.readLine

object PersistenceService {
  def main(args: Array[String]): Unit = {
    PersistenceApi().start() // on Port 9083
    
    val mongo = given_IPersistence
    val realPersistence = mongo.asInstanceOf[Persistence]
    realPersistence.createTables


/*    val mongoTest = given_IPersistence
    val jsonPersistance = new de.htwg.sa.minesweeper.persistence.persistenceComponent.persistenceJsonImpl.Persistence()
    val fieldOption = jsonPersistance.loadField
    val field = fieldOption.get

    mongoTest.saveField(field)
    val maybeMongoField = mongoTest.loadField
    val mongoField = maybeMongoField.get
    val realField = mongoField.asInstanceOf[Field]*/

    //println(realField.matrix.size)

/*    val mongoInstance = new Persistence()
    val game = Game(6, 5, 0, "Playing")
    val game3 = Game(10, 9, 0, "Playing")
    val gameDao = new GameDao(mongoInstance.db)
    val resultGame = gameDao.findById(5)
    println(resultGame.side) // 77
    gameDao.update(1, game3)*/

    //mongoInstance.insertGame(game)
    //val game2 = mongoInstance.loadGame(5)
    //println(game2.get.side)

    println("Press RETURN to stop...")
    readLine() // Keep the application alive until user presses return
  }
}
