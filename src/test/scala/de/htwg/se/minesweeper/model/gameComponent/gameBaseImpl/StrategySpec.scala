package de.htwg.se.minesweeper.model.gameComponent.gameBaseImpl

import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpec
import de.htwg.se.minesweeper.model.gameComponent.IGame
import de.htwg.se.minesweeper.Default


class StrategySpec extends AnyWordSpec{

    "it" should {
        "use the first move strategy" in {
            val decider = new Decider()
            var game = new Game(Status.Playing, 2, 1)
            game.setField()
            val unsichtbar = Matrix(Vector(Vector(Symbols.One, Symbols.One), Vector(Symbols.One, Symbols.Bomb)))
            val sichtbar = Matrix(Vector(Vector(Symbols.Covered, Symbols.Covered), Vector(Symbols.Covered, Symbols.Covered)))
            val field = Field(sichtbar, unsichtbar)

            val result = decider.evaluateStrategy(true, 1, 1, field, game)
            assert(result.showVisibleCell(1, 1) == Symbols.One)

        }
    }

    "the class" should {
        "use the normal move strategy" in {
            var spielbrett = GameState
            val decider2 = new Decider()
            var game2 = new Game(Status.Playing, 2, 1)
            game2.setField()
            val unsichtbar = Matrix(Vector(Vector(Symbols.One, Symbols.One), Vector(Symbols.One, Symbols.Bomb)))
            val sichtbar = Matrix(Vector(Vector(Symbols.Covered, Symbols.Covered), Vector(Symbols.Covered, Symbols.Covered)))
            val field2 = Field(sichtbar, unsichtbar)

            val result2 = decider2.evaluateStrategy(false, 1, 1, field2, game2)
            assert(result2.showVisibleCell(1, 1) != Symbols.One)

        }
    }

    
}