package de.htwg.se.minesweeper.model.fileIoComponent.fileIoXmlImpl

import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpec

import de.htwg.se.minesweeper.model.gameComponent.gameBaseImpl.{Field, Game, Matrix, Playfield, Status, Symbols}
import de.htwg.se.minesweeper.model.fileIoComponent.fileIoXmlImpl.FileIO
import de.htwg.se.minesweeper.model.gameComponent.IGame
import de.htwg.se.minesweeper.model.gameComponent.IField
import de.htwg.se.minesweeper.Default

class FileIOXmlSpec extends AnyWordSpec {
  "A FileIO" when {

    "save and load Game" should {
      val fileIO1 = new FileIO
      var game: IGame = new Game(Status.Playing)
      game.setBombs(10)
      game.setSide(9)
      game.setTime(10)

      "save the game status, bombs, side and time and reload it" in {
        fileIO1.saveGame(game)
        val loadGameOption = fileIO1.loadGame
        val loadGameTest = loadGameOption.get
        loadGameTest.getBombs should be (10)
        loadGameTest.getSide should be (9)
        loadGameTest.getTime should be (10)
      }
    }

    "save and load Field" should {
      val fileIO2 = new FileIO
      var field: IField = new Field(1, Symbols.Covered)
      field = field.put(Symbols.Bomb, 0, 0)

      "save the field and reload it" in {
        fileIO2.saveField(field)
        val loadFieldTest = fileIO2.loadField2.get
        loadFieldTest.getFieldSize should be (1)
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

    "def stateExtractor" should {
      val fileIO4 = new FileIO

      "return the correct state" in {
        fileIO4.stateExtractor("Playing") should be (Status.Playing)
        fileIO4.stateExtractor("Won") should be (Status.Won)
        fileIO4.stateExtractor("Lost") should be (Status.Lost)
      }
    }


  }


}

