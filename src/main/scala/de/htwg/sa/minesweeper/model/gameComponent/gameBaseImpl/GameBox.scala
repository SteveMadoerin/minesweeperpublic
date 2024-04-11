package de.htwg.sa.minesweeper.model.gameComponent.gameBaseImpl


case class GameBox(game: Option[Game]):

    def insertBomb(newBombs: Int): GameBox = game match
        case Some(game: Game) => copy(Some(game.insertBomb(newBombs)))
        case None => copy(None)

    def insertSide(newSide: Int): GameBox = game match
        case Some(game: Game) => copy(Some(game.insertSide(newSide)))
        case None => copy(None)

    def insertTime(newTime: Int): GameBox = game match
        case Some(game: Game) => copy(Some(game.insertTime(newTime)))
        case None => copy(None)

    def insertBoard(newBoard: String): GameBox = game match
        case Some(game: Game) => copy(Some(game.insertBoard(newBoard)))
        case None => copy(None)

end GameBox

