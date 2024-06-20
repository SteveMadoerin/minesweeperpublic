package de.htwg.sa.minesweeper.persistence.fileIoComponent.fileIoXmlImpl

import de.htwg.sa.minesweeper.persistence.entity.{Field, Game, IField, IGame, Matrix}
import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpec

class PersistenceIOXmlSpec extends AnyWordSpec {


  "A FileIO" when {

     "save and load Game" should {
      val fileIO1 = new de.htwg.sa.minesweeper.persistence.persistenceComponent.persistenceJsonImpl.Persistence()
       val game: IGame = Game(10, 9, 0, "Playing")

      "save the game status, bombs, side and time and reload it" in {
        fileIO1.saveGame(game)
        val loadGameOption = fileIO1.loadGame
        val loadGameTest = loadGameOption.get
        loadGameTest.bombs should be (10)
        loadGameTest.side should be (9)
        loadGameTest.time should be (0)
      }
    }

     "save and load Field" should {
      val fileIO2 = de.htwg.sa.minesweeper.persistence.persistenceComponent.persistenceJsonImpl.Persistence()
      val field: IField = Field(Matrix(Vector(Vector("~", "~"), Vector("~", "~"))), Matrix(Vector(Vector("0", "*"),Vector("0", "0"))))

      "save the field and reload it" in {
        fileIO2.saveField(field)
        val loadFieldTest = fileIO2.loadField
        loadFieldTest.size should be (1)
      }
    }

  }
}