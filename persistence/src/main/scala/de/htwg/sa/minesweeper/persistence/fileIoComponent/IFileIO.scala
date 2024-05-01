package de.htwg.sa.minesweeper.persistence.fileIoComponent

import de.htwg.sa.minesweeper.model.gameComponent.{IGame, IField}
import de.htwg.sa.minesweeper.model.gameComponent.gameBaseImpl.GameBox

trait IFileIO {
    def loadGame: Option[IGame]
    //def loadGame: GameBox
    def saveGame(game: IGame): Unit

    def loadField: Option[IField]
    def saveField(field: IField): Unit

    def loadPlayerScores(filePath: String): Seq[(String, Int)]
    def savePlayerScore(playerName: String, score: Int, filePath: String): Unit 
}