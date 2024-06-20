package de.htwg.sa.minesweeper.model.gameComponent.gameBaseImpl

import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpec

class FieldCreatorTemplateSpec extends AnyWordSpec{

    "it" should {
        "ceate an empty field" in {
            val testfield = new EmptyField
            val testGame = Game(3, 1, 0, "Playing")
            
            val result = testfield.newField(3, testGame)
            result.showVisibleCell(1,1) should be ("~")
        }
    }

    "the Template method" should{
        "create a playfield" in {
            val testfield2 = new Playfield
            val testGame2 = new Game(3, 3, 0, "Playing")

            val result2 = testfield2.newField(3, testGame2)
            result2.open(1,1, testGame2) should not be ("~")
        }
    }

    "the Template method .." should{
        "create a minefield" in {
            val testfield3 = new Minefield
            val testGame3 = new Game(3, 3, 0, "Playing")

            val result2 = testfield3.newField(3, testGame3)
            result2.open(1,1, testGame3) should not be ("~")
        }
    }
}