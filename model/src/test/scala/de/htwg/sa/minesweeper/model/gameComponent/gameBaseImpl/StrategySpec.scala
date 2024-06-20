package de.htwg.sa.minesweeper.model.gameComponent.gameBaseImpl

import de.htwg.sa.minesweeper.model.gameComponent.IGame
import org.scalatest.matchers.should.Matchers.*
import org.scalatest.wordspec.AnyWordSpec


class StrategySpec extends AnyWordSpec{

    "it" should {
        "use the first move strategy" in {
            val decider = new Decider()
            val game = Game(10, 9, 0, "Playing")
            val unsichtbar = Matrix(Vector(Vector("1", "1"), Vector("1", "1")))
            val sichtbar = Matrix(Vector(Vector("~", "~"), Vector("~", "~")))
            val field = Field(sichtbar, unsichtbar)

            val result = decider.evaluateStrategy(true, 1, 1, field, game)
            val (game2, field2) = result
            assert(field2.showVisibleCell(1, 1) != "~")
        }
    }

    "the class" should {
        "use the normal move strategy" in {
            val decider2 = new Decider()
            val game2 = Game(2, 1, 0, "Playing")
            val unsichtbar = Matrix(Vector(Vector("1", "1"), Vector("1", "~")))
            val sichtbar = Matrix(Vector(Vector("~", "~"), Vector("~", "~")))
            val field2 = Field(sichtbar, unsichtbar)

            val result2 = decider2.evaluateStrategy(false, 1, 1, field2, game2)
            val (game3, field3) = result2
            assert(field3.showVisibleCell(1, 1) != "1")
        }
    }

    
}