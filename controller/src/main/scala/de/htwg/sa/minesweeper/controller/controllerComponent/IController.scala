package de.htwg.sa.minesweeper.controller.controllerComponent

import de.htwg.sa.minesweeper.util.{Observable, Move}
import de.htwg.sa.minesweeper.entity.{FieldDTO, GameDTO}

trait IController extends Observable{
    def doMove(b: Boolean, move: Move, game: GameDTO): FieldDTO
    def loadGame: Unit
    def saveGame: Unit
    def gameOver: Unit
    def openRec(x: Int, y: Int, field: FieldDTO): FieldDTO
    def helpMenu: Unit
    def helpMenuRest: String
    def fieldToString: String
    def cheat: Unit
    def cheatRest: String
    
    def checkGameOver(status: String): Boolean
    def checkGameOverGui: Boolean
    def newGameGUI: Unit
    def newGameForGui(side: Int, bombs: Int): Unit
    def newGameField(optionString: Option[String]): Unit
    def newGame(side: Int, bombs: Int): Unit

    def makeAndPublish(makeThis: (Boolean, Move, GameDTO) => FieldDTO, b: Boolean, move: Move, game: GameDTO): Unit
    def makeAndPublish(makeThis: Move => FieldDTO, move: Move): Unit
    def makeAndPublish(makeThis: => FieldDTO): FieldDTO

    def loadPlayerScores: Seq[(String, Int)]
    def saveScoreAndPlayerName(playerName: String, saveScore: Int, filePath: String): Unit
   
    def field: FieldDTO
    def game: GameDTO

    def put(move: Move): FieldDTO
    def undo: FieldDTO
    def redo: FieldDTO

    def exit: Unit
    def saveTime(currentTime: Int): Unit
}
