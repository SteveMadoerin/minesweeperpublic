package de.htwg.sa.minesweeper.model.gameComponent.gameBaseImpl

import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpec
import de.htwg.sa.minesweeper.model.gameComponent.IGame
import de.htwg.sa.minesweeper.Default
import de.htwg.sa.minesweeper.model.gameComponent.gameBaseImpl.{Decider, Game, Field, Matrix}


class StrategySpec extends AnyWordSpec{

    "it" should {
        "use the first move strategy" in {
            val decider = new Decider()
            var game = new Game(10, 9, 0, "Playing")
            //game.setField()
            val unsichtbar = Matrix(Vector(Vector(Symbols.One, Symbols.One), Vector(Symbols.One, Symbols.One)))
            val sichtbar = Matrix(Vector(Vector(Symbols.Covered, Symbols.Covered), Vector(Symbols.Covered, Symbols.Covered)))
            val field = Field(sichtbar, unsichtbar)

            val result = decider.evaluateStrategy(true, 1, 1, field, game)
            val (game2, field2) = result
            assert(field2.showVisibleCell(1, 1) != Symbols.Bomb)

        }
    }

    "the class" should {
        "use the normal move strategy" in {
            val decider2 = new Decider()
            var game2 = new Game(2, 1, 0, "Playing")
            //game2.setField()
            val unsichtbar = Matrix(Vector(Vector(Symbols.One, Symbols.One), Vector(Symbols.One, Symbols.Bomb)))
            val sichtbar = Matrix(Vector(Vector(Symbols.Covered, Symbols.Covered), Vector(Symbols.Covered, Symbols.Covered)))
            val field2 = Field(sichtbar, unsichtbar)

            val result2 = decider2.evaluateStrategy(false, 1, 1, field2, game2)
            val (game3, field3) = result2
            assert(field3.showVisibleCell(1, 1) != Symbols.One)

        }
    }

    
}