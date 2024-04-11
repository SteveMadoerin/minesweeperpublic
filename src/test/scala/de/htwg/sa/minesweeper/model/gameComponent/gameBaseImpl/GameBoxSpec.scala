package de.htwg.sa.minesweeper.model.gameComponent.gameBaseImpl

import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpec
import de.htwg.sa.minesweeper.model.gameComponent.gameBaseImpl.{Game, GameBox}

class GameBoxSpec extends AnyWordSpec{

      "A GameBox" when {
    "non-empty" should {
      val existingGame = Game(bombs = 5, side = 10, time = 0, board = "Playing")
      val gameBox = GameBox(Some(existingGame))

      "update the number of bombs when insertBomb is invoked" in {
        val newBombs = 10
        val updatedGameBox = gameBox.insertBomb(newBombs)
        updatedGameBox.game.map(_.bombs) shouldBe Some(newBombs)
      }

      "update the side length of the game board when insertSide is invoked" in {
        val newSide = 15
        val updatedGameBox = gameBox.insertSide(newSide)
        updatedGameBox.game.map(_.side) shouldBe Some(newSide)
      }

      "update the time of the game when insertTime is invoked" in {
        val newTime = 100
        val updatedGameBox = gameBox.insertTime(newTime)
        updatedGameBox.game.map(_.time) shouldBe Some(newTime)
      }

      "update the state of the game board when insertBoard is invoked" in {
        val newBoard = "Won"
        val updatedGameBox = gameBox.insertBoard(newBoard)
        updatedGameBox.game.map(_.board) shouldBe Some(newBoard)
      }
    }

    "empty" should {
      val emptyGameBox = GameBox(None)

      "not update the number of bombs when insertBomb is invoked" in {
        val newBombs = 10
        val updatedGameBox = emptyGameBox.insertBomb(newBombs)
        updatedGameBox.game shouldBe None
      }

      "not update the side length of the game board when insertSide is invoked" in {
        val newSide = 15
        val updatedGameBox = emptyGameBox.insertSide(newSide)
        updatedGameBox.game shouldBe None
      }

      "not update the time of the game when insertTime is invoked" in {
        val newTime = 100
        val updatedGameBox = emptyGameBox.insertTime(newTime)
        updatedGameBox.game shouldBe None
      }

      "not update the state of the game board when insertBoard is invoked" in {
        val newBoard = "Won"
        val updatedGameBox = emptyGameBox.insertBoard(newBoard)
        updatedGameBox.game shouldBe None
      }
    }
  }
}
