package de.htwg.se.minesweeper.model.gameComponent.gameBaseImpl

import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpec
import de.htwg.se.minesweeper.Default

class FieldCreatorTemplateSpec extends AnyWordSpec{

    "it" should {
        "ceate an empty field" in {
            val testfield = new EmptyField
            val testGame = new Game(Status.Playing)
            testGame.side = 3
            testGame.bombs = 1

            val result = testfield.newField(3, testGame)
            result.get(1,1) should be (Symbols.Covered)
        }
    }

    "the Template method" should{
        "create a playfield" in {
            var spielbrett1 = GameState
            val testfield2 = new Playfield
            val testGame2 = new Game(Status.Playing)
            testGame2.setField()
            testGame2.side = 3
            testGame2.bombs = 1

            val result2 = testfield2.newField(3, testGame2)
            result2.open(1,1, testGame2) should not be (Symbols.Covered)
        }
    }

    "the Template method .." should{
        "create a minefield" in {
            var spielbrett2 = GameState
            val testfield3 = new Minefield
            val testGame3 = new Game(Status.Playing)
            testGame3.setField()
            testGame3.side = 3
            testGame3.bombs = 1

            val result2 = testfield3.newField(3, testGame3)
            result2.open(1,1, testGame3) should not be (Symbols.Covered)
        }
    }
}