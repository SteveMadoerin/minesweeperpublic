package de.htwg.se.minesweeper.controller.controllerComponent.controllerBaseImpl

import de.htwg.se.minesweeper.controller.controllerComponent.IController
import de.htwg.se.minesweeper.model.gameComponent.gameBaseImpl._
import de.htwg.se.minesweeper.model.gameComponent._
import de.htwg.se.minesweeper.util.{Observable, Move, UndoRedoManager, NewEvent}

import de.htwg.se.minesweeper.Default.{given}
import de.htwg.se.minesweeper.model.fileIoComponent.IFileIO
import de.htwg.se.minesweeper.model.gameComponent.gameBaseImpl.GameState
import de.htwg.se.minesweeper.Default

class Controller(using var game: IGame, var file: IFileIO) extends IController with Observable:
    var field: IField = game.getField
    var spielbrett = GameState
    var decider = new Decider()
    val undoRedoManager = new UndoRedoManager[IField]
    
    def doMove(b: Boolean, move: Move, game: IGame) = 
        field = decider.evaluateStrategy(b, move.x, move.y, field, game)
        if(field.showInvisibleCell(move.y, move.x) == Symbols.Zero){field}
        else{undoRedoManager.doStep(field, DoCommand(move))}

    def loadGame =

        val gameOption = file.loadGame
        gameOption match {
            case Some(game) => this.game = game
            case None =>
        }
        GameState.handle(PlayEvent())

        val fieldOption = file.loadField2
        fieldOption match {
            case Some(field) => this.field = field
            case None =>
        }

        notifyObservers(NewEvent.Load)
    
    def saveGame =

        notifyObservers(NewEvent.SaveTime)
        file.saveGame(game)
        file.saveField(field)
        GameState.handle(PlayEvent())
        notifyObservers(NewEvent.Save)

    def exit =
        notifyObservers(NewEvent.Exit)

    def gameOver =
        field = field.gameOverField
        notifyObservers(NewEvent.GameOver)

    def flag(x: Int, y: Int) = 
        field = field.putFlag(x,y)
        notifyObservers(NewEvent.Next)

    def unflag(x: Int, y: Int) = 
        field = field.removeFlag(x,y)
        notifyObservers(NewEvent.Next)
    
    def openRec(x: Int, y: Int, field: IField): IField = 
        val feld = undoRedoManager.doStep(field, DoCommand(Move("recursion", x, y)))
        feld

    def helpMenu = 
        game.helpMessage
        println(field.toString)
        notifyObservers(NewEvent.Help)
    
    def cheat = 
        field.reveal
        notifyObservers(NewEvent.Cheat)
    
    def checkGameOver =
        game.checkExit

    def newGameGUI =
        GameState.handle(PlayEvent())
        notifyObservers(NewEvent.Input)

    def newGameField(optionString: Option[String]) =
        field = game.prepareBoard(optionString, game) // NEW
        notifyObservers(NewEvent.NewGame)

    def newGame(side: Int, bombs: Int) =
        GameState.handle(PlayEvent())
        game.setSideAndBombs(side, bombs)
        field = game.createField
        notifyObservers(NewEvent.NewGame)

    def makeAndPublish(makeThis: (Boolean, Move, IGame) => IField, b: Boolean, move: Move, game: IGame): Unit =
        field = makeThis(b, move, game)
        if (field.showInvisibleCell(move.y, move.x) == Symbols.Zero){field = openRec(move.x,move.y,field)}
        val firstOrNext = if (b) NewEvent.Start else NewEvent.Next
        notifyObservers(firstOrNext)

    def makeAndPublish(makeThis: Move => IField, move: Move): Unit =
        field = makeThis(move)
        notifyObservers(NewEvent.Next)

    def makeAndPublish(makeThis: => IField) =
        field = makeThis
        notifyObservers(NewEvent.Next)

    def saveScoreAndPlayerName(playerName: String, saveScore: Int, filePath: String) = {
        file.savePlayerScore(playerName, saveScore, filePath)
    }

    def loadPlayerScores(filePath: String) = {
        file.loadPlayerScores(filePath)
    }

    def showVisibleCell(x: Int, y: Int): String = field.showVisibleCell(x,y).toString
    //def showInvisibleCell(x: Int, y: Int): String = field.showInvisibleCell(x,y).toString

    def getFieldSize: Int = field.getMatrix.size
    def getSpielbrettState: Status = spielbrett.state
    def getControllerField: IField = field
    def getControllerGame: IGame = game
    override def getControllerGameInterface: IGame  = game
    
    def put(move: Move): IField = undoRedoManager.doStep(field, DoCommand(move))
    def undo: IField = undoRedoManager.undoStep(field)
    def redo: IField = undoRedoManager.redoStep(field)

    override def toString = field.toString