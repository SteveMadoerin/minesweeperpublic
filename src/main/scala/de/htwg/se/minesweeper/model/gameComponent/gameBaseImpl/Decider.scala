package de.htwg.se.minesweeper.model.gameComponent.gameBaseImpl

import de.htwg.se.minesweeper.model.gameComponent._

class Decider():
    private var strategy: OpenStrategy = _

    def evaluateStrategy(expression: Boolean, x: Int, y: Int, field: IField, game: IGame): IField = {
        if (expression) {
            strategy = new FirstMove
            strategy.show(x, y, field, game)
        } else {
            strategy = new NormalMove
            strategy.show(x, y, field, game)
        }
    }
