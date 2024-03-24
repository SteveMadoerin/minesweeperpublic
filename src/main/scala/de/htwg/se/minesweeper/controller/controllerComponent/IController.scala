package de.htwg.se.minesweeper.controller.controllerComponent

import de.htwg.se.minesweeper.model.gameComponent._
import de.htwg.se.minesweeper.model.gameComponent.gameBaseImpl._
import de.htwg.se.minesweeper.util.{Observable, Move}


trait IController extends Observable{
    def doMove(b: Boolean, move: Move, game: IGame): IField
    def loadGame: Unit
    def saveGame: Unit
    def gameOver: Unit
    def flag(x: Int, y: Int): Unit
    def unflag(x: Int, y: Int): Unit
    def openRec(x: Int, y: Int, field: IField): IField
    def helpMenu: Unit
    def cheat: Unit
    def checkGameOver: Boolean
    def newGameGUI: Unit
    def newGameField(optionString: Option[String]): Unit
    def newGame(side: Int, bombs: Int): Unit

    def makeAndPublish(makeThis: (Boolean, Move, IGame) => IField, b: Boolean, move: Move, game: IGame): Unit
    def makeAndPublish(makeThis: Move => IField, move: Move): Unit
    def makeAndPublish(makeThis: => IField): Unit

    def loadPlayerScores(filePath: String): Seq[(String, Int)]
    def saveScoreAndPlayerName(playerName: String, saveScore: Int, filePath: String): Unit

    def showVisibleCell(x: Int, y: Int): String
   
    def getFieldSize: Int
    def getSpielbrettState: Status
    def getControllerField: IField
    def getControllerGame: IGame

    def put(move: Move): IField
    def undo: IField
    def redo: IField

    def exit: Unit
}
