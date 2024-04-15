package de.htwg.sa.minesweeper.persistence.fileIoComponent.fileIoXmlImpl

import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpec

import de.htwg.sa.minesweeper.model.gameComponent.gameBaseImpl.{Field, Game, Matrix, Playfield, Symbols}
import de.htwg.sa.minesweeper.persistence.fileIoComponent.fileIoXmlImpl.FileIO
import de.htwg.sa.minesweeper.model.gameComponent.IGame
import de.htwg.sa.minesweeper.model.gameComponent.IField
//import de.htwg.sa.minesweeper.Default
import de.htwg.sa.minesweeper.model.gameComponent.config.Default

class FileIOXmlSpec extends AnyWordSpec {


  "A FileIO" when {

    "save and load Game" should {
      val fileIO1 = new FileIO
      var game: IGame = new Game(10, 9, 0, "Playing")

      "save the game status, bombs, side and time and reload it" in {
        fileIO1.saveGame(game)
        val loadGameOption = fileIO1.loadGame
        val loadGameTest = loadGameOption.game.get
        loadGameTest.bombs should be (10)
        loadGameTest.side should be (9)
        loadGameTest.time should be (0)
      }
    }



    "save and load Field" should {
      val fileIO2 = new FileIO
      var field: IField = new Field(1, Symbols.Covered)
      field = field.put(Symbols.Bomb, 0, 0)

      "save the field and reload it" in {
        fileIO2.saveField(field)
        val loadFieldTest = fileIO2.loadField
        loadFieldTest.size should be (1)
      }
    }

    "def stringToSymbols" should {
      val fileIO3 = new FileIO

      "return the correct symbol" in {

        fileIO3.stringToSymbols("*") should be (Symbols.Bomb)
        fileIO3.stringToSymbols("~") should be (Symbols.Covered)
        fileIO3.stringToSymbols("F") should be (Symbols.F)
        fileIO3.stringToSymbols("1") should be (Symbols.One)
        fileIO3.stringToSymbols("2") should be (Symbols.Two)
        fileIO3.stringToSymbols("0") should be (Symbols.Zero)
        fileIO3.stringToSymbols(" ") should be (Symbols.Empty)
        fileIO3.stringToSymbols("3") should be (Symbols.Three)
        fileIO3.stringToSymbols("4") should be (Symbols.Four)
        fileIO3.stringToSymbols("5") should be (Symbols.Five)
        fileIO3.stringToSymbols("6") should be (Symbols.Six)
        fileIO3.stringToSymbols("7") should be (Symbols.Seven)
        fileIO3.stringToSymbols("8") should be (Symbols.Eight)
        fileIO3.stringToSymbols("anyString") should be (Symbols.E)
      }
    }

  }
}