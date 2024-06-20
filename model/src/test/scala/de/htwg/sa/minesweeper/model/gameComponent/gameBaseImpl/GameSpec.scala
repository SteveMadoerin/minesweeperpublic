package de.htwg.sa.minesweeper.model.gameComponent.gameBaseImpl

import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpec
import java.io.ByteArrayOutputStream
import de.htwg.sa.minesweeper.model.gameComponent.gameBaseImpl.{Game, Field}


class GameSpec extends AnyWordSpec {

    "Method replaceBomb" should {
        val testGame10 = new Game(2, 3, 0, "Playing")

        val sicht1 = new Matrix(4, "~")
        val unsicht1 = new Matrix(4, " ")
        val unsicht2 = unsicht1.replaceCell(1, 1, "*")
        val testField1 = new Field(sicht1, unsicht2)


        "replace matrix and ensure the player doesn't click on bomb at first move" in{

            val result33 = testGame10.replaceBomb(1, 1, testField1)
            result33.showInvisibleCell(1, 1) should not be ("*")
        }

    }

    "Premier move" should{

        val game42 = new Game(1, 3, 0, "Playing")

        val sicht11 = new Matrix(4, "~")
        val unsicht11 = new Matrix(4, " ")
        val unsicht22 = unsicht11.replaceCell(1, 1, "*")
        val preparedField = new Field(sicht11, unsicht22)

        "guarante that you do not loose on first move" in{
            val result42 = game42.premierMove(1, 1, preparedField)
            result42.showInvisibleCell(1, 1) should not be ("*")
        }
    }

    "Method check exit" should {
        "return false game is in Status playing" in {
            var testGame55 = new Game(1, 3, 0, "Playing")	
            val testB = "Playing"

            testGame55.checkExit(testB) should be (false)
        }
    }
    
    "def calcMineAndFlag" should {
        val testGame = new Game(1, 3, 0, "Playing")

        val sicht = new Matrix(4, "~")
        val unsicht = new Matrix(4, "0")
        val unsicht2 = unsicht.replaceCell(1, 1, "*")
        val testField = new Field(sicht, unsicht2)

        "return the number of flags minus bombs" in {
            val result = testGame.calcMineAndFlag(testField.matrix)
            result should be (1)
        }
    }

    "optionToList" should {
        val testGame66 = new Game(1, 3, 0, "Playing")	

        "return List(5, 5) when given Some(\"SuperEasy\")" in {
            testGame66.optionToList(Some("SuperEasy")) shouldEqual List(5, 5)
        }

        "return List(9, 10) when given Some(\"Easy\")" in {
            testGame66.optionToList(Some("Easy")) shouldEqual List(9, 10)
        }

        "return List(15, 40) when given Some(\"Medium\")" in {
            testGame66.optionToList(Some("Medium")) shouldEqual List(15, 40)
        }

        "return List(19, 85) when given Some(\"Hard\")" in {
            testGame66.optionToList(Some("Hard")) shouldEqual List(19, 85)
        }

        "return the default List(9, 10) when given None" in {
            testGame66.optionToList(None) shouldEqual List(9, 10)
        }

        "return the default List(9, 10) when given Some with an unrecognized value" in {
            testGame66.optionToList(Some("Unrecognized")) shouldEqual List(9, 10)
        }
    }

    "def getGame" should {
        val testGame = new Game(1, 3, 0, "Playing")

        "return the game" in {
            val result = testGame
            result should be (testGame)
        }
    }

    "def setState" should {
        var testGame67 = new Game(1, 3, 0, "Playing")	

        "return the game" in {
            var testGameTmp = testGame67.copy(board = "Playing")
            testGameTmp.board should be ("Playing")
            
        }
    
    }

    "Game object" should {
        val testGame = Game(1, 3, 0, "Playing")

        "return the game" in {
            val result = testGame
            result should be (testGame)
        }
    }

   
    "insertBomb is invoked" should {
        "update the number of bombs in the game" in {
            val game = Game(bombs = 5, side = 10, time = 0, board = "Playing")
            val newBombs = 10
            val updatedGame = game.insertBomb(newBombs)
            updatedGame.bombs shouldBe newBombs
        }
    }

    "insertSide is invoked" should {
        "update the side length of the game board" in {
            val game = Game(bombs = 5, side = 10, time = 0, board = "Playing")
            val newSide = 15
            val updatedGame = game.insertSide(newSide)
            updatedGame.side shouldBe newSide
        }
    }

    "insertTime is invoked" should {
        "update the time of the game" in {
            val game = Game(bombs = 5, side = 10, time = 0, board = "Playing")
            val newTime = 100
            val updatedGame = game.insertTime(newTime)
            updatedGame.time shouldBe newTime
        }
    }

    "insertBoard is invoked" should {
        "update the state of the game board" in {
            val game = Game(bombs = 5, side = 10, time = 0, board = "Playing")
            val newBoard = "Won"
            val updatedGame = game.insertBoard(newBoard)
            updatedGame.board shouldBe newBoard
        }
    }

}