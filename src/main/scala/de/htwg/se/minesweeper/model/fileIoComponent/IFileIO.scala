package de.htwg.se.minesweeper.model.fileIoComponent

import de.htwg.se.minesweeper.model.gameComponent.{IGame, IField}

trait IFileIO {
    def loadGame: Option[IGame]
    def saveGame(game: IGame): Unit

    def loadField2: Option[IField]
    def saveField(field: IField): Unit

    def loadPlayerScores(filePath: String): Seq[(String, Int)]
    def savePlayerScore(playerName: String, score: Int, filePath: String): Unit 
}