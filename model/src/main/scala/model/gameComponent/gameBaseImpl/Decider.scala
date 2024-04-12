package model.gameComponent.gameBaseImpl

import model.gameComponent._

class Decider():
    
    def evaluateStrategy(expression: Boolean, x: Int, y: Int, field: IField, game: IGame): (IGame, IField) = {
        if (expression) {
            val strategy: OpenStrategy = new FirstMove
            strategy.show(x, y, field, game)
        } else {
            val strategy: OpenStrategy  = new NormalMove
            strategy.show(x, y, field, game)
        }
    }

end Decider