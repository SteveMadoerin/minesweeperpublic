package de.htwg.sa.minesweeper.persistence

import de.htwg.sa.minesweeper.persistence.persistenceComponent
import de.htwg.sa.minesweeper.persistence.persistenceComponent.{GameTable, PersistenceApi, Slick}
import de.htwg.sa.minesweeper.persistence.entity.Game
import de.htwg.sa.minesweeper.persistence.persistenceComponent.config.Default.given
import de.htwg.sa.minesweeper.persistence.persistenceComponent.persistenceSlickImpl.Dao.GameDao
import slick.dbio.DBIO
import slick.jdbc.JdbcBackend.Database
import slick.lifted.TableQuery
import slick.jdbc.PostgresProfile.api._

import scala.io.StdIn.readLine

object PersistenceService {
  def main(args: Array[String]): Unit = {
    PersistenceApi().start() // on Port 9083
    println("Press RETURN to stop...")
    readLine() // Keep the application alive until user presses return
  }
}
