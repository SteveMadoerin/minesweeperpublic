package de.htwg.se.minesweeper.model.gameComponent.gameBaseImpl


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
/* case class GameBox(game: Option[Game] = None) {
    def apply(f: Game => Game) = copy(game = game.map(f))

    def insertBomb(newBombs: Int) = apply(_.insertBomb(newBombs))
    def insertSide(newSide: Int) = apply(_.insertSide(newSide))
    def insertTime(newTime: Int) = apply(_.insertTime(newTime))
    def insertBoard(newBoard: String) = apply(_.insertBoard(newBoard))
}

end GameBox */
