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
    
/*    val mongo = given_IPersistence
    val realPersistence = mongo.asInstanceOf[Persistence]
    realPersistence.createTables*/
    
    

    

    println("Press RETURN to stop...")
    readLine() // Keep the application alive until user presses return
  }
}
