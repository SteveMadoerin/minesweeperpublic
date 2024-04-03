package de.htwg.se.minesweeper.util

case class MoveBox(move: Option[Move]):
    
    def validate: MoveBox = move match
        case Some(move: Move) => copy(Some(move.validate))
        case None => copy(None)

end MoveBox