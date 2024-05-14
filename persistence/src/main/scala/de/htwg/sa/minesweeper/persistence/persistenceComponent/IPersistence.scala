package de.htwg.sa.minesweeper.persistence.persistenceComponent

import de.htwg.sa.minesweeper.persistence.entity.{IField, IGame}

trait IPersistence {
    def loadGame: Option[IGame]
    
    def saveGame(game: IGame): Unit

    def loadField: Option[IField]

    def saveField(field: IField): Unit

    def loadPlayerScores(filePath: String): Seq[(String, Int)]

    def savePlayerScore(playerName: String, score: Int, filePath: String): Unit
}
