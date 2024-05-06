package de.htwg.sa.minesweeper.persistence.fileIoComponent.fileIoXmlImpl

import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpec

import de.htwg.sa.minesweeper.model.gameComponent.gameBaseImpl.{Field, Game, Matrix, Playfield}
import de.htwg.sa.minesweeper.persistence.fileIoComponent.fileIoXmlImpl.FileIO
import de.htwg.sa.minesweeper.model.gameComponent.IGame
import de.htwg.sa.minesweeper.model.gameComponent.IField
//import de.htwg.sa.minesweeper.Default
import de.htwg.sa.minesweeper.model.gameComponent.config.Default

class FileIOXmlSpec extends AnyWordSpec {


  "A FileIO" when {

/*     "save and load Game" should {
      val fileIO1 = new FileIO
      var game: IGame = new Game(10, 9, 0, "Playing")

      "save the game status, bombs, side and time and reload it" in {
        fileIO1.saveGame(game)
        val loadGameOption = fileIO1.loadGame
        val loadGameTest = loadGameOption.get
        loadGameTest.bombs should be (10)
        loadGameTest.side should be (9)
        loadGameTest.time should be (0)
      }
    } */



/*     "save and load Field" should {
      val fileIO2 = new FileIO
      var field: IField = new Field(1, "~")
      field = field.put("*", 0, 0)

      "save the field and reload it" in {
        fileIO2.saveField(field)
        val loadFieldTest = fileIO2.loadField
        loadFieldTest.size should be (1)
      }
    } */

  }
}