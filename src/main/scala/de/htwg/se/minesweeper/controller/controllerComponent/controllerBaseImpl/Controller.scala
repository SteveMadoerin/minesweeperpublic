package de.htwg.se.minesweeper.controller.controllerComponent.controllerBaseImpl

import de.htwg.se.minesweeper.controller.controllerComponent.IController
import de.htwg.se.minesweeper.model.gameComponent.gameBaseImpl._
import de.htwg.se.minesweeper.model.gameComponent._
import de.htwg.se.minesweeper.util.{Observable, Move, UndoRedoManager, Event}

import de.htwg.se.minesweeper.Default.{given}
import de.htwg.se.minesweeper.model.fileIoComponent.IFileIO
import de.htwg.se.minesweeper.Default

class Controller(using var game: IGame, var file: IFileIO) extends IController with Observable:
    var field: IField = game.hyperField
    //var spielbrettState = game._board
    var spielbrettState = "Playing"
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
        game.handleGameState("Playing")

        val fieldOption = file.loadField2
        fieldOption match {
            case Some(field) => this.field = field
            case None =>
        }

        notifyObservers(Event.Load)
    
    def saveGame =

        notifyObservers(Event.SaveTime)
        file.saveGame(game)
        file.saveField(field)
        game.handleGameState("Playing")
        notifyObservers(Event.Save)

    def exit =
        notifyObservers(Event.Exit)

    def gameOver =
        field = field.gameOverField
        notifyObservers(Event.GameOver)

    def flag(x: Int, y: Int) = 
        field = field.putFlag(x,y)
        notifyObservers(Event.Next)

    def unflag(x: Int, y: Int) = 
        field = field.removeFlag(x,y)
        notifyObservers(Event.Next)
    
    def openRec(x: Int, y: Int, field: IField): IField = 
        val feld = undoRedoManager.doStep(field, DoCommand(Move("recursion", x, y)))
        feld

    def helpMenu = 
        game.helpMessage
        println(field.toString)
        notifyObservers(Event.Help)
    
    def cheat = 
        field.reveal
        notifyObservers(Event.Cheat)
    
    def checkGameOver =
        game.checkExit

    def newGameGUI =
        game.handleGameState("Playing")
        notifyObservers(Event.Input)

    
    //for GUI
    def newGameField(optionString: Option[String]) =
        game.handleGameState("Playing")
        val (feld, spiel) = game.prepareBoard(optionString, game)
        field = feld
        game = spiel
        //field = game.prepareBoard(optionString, game) // NEW

        notifyObservers(Event.NewGame)

    def newGame(side: Int, bombs: Int) =
        game.handleGameState("Playing")
        val tempGame: Game = game.asInstanceOf[Game]
        val gameCopy = tempGame.copy(side, bombs)
        val newGame: IGame = gameCopy
        game = newGame
        //game.setSideAndBombs(side, bombs)
        field = game.createField
        notifyObservers(Event.NewGame)

    def makeAndPublish(makeThis: (Boolean, Move, IGame) => IField, b: Boolean, move: Move, game: IGame): Unit =
        field = makeThis(b, move, game)
        if (field.showInvisibleCell(move.y, move.x) == Symbols.Zero){field = openRec(move.x,move.y,field)}
        val firstOrNext = if (b) Event.Start else Event.Next
        notifyObservers(firstOrNext)

    def makeAndPublish(makeThis: Move => IField, move: Move): Unit =
        field = makeThis(move)
        notifyObservers(Event.Next)

    def makeAndPublish(makeThis: => IField) =
        field = makeThis
        notifyObservers(Event.Next)

    def saveScoreAndPlayerName(playerName: String, saveScore: Int, filePath: String) = {
        file.savePlayerScore(playerName, saveScore, filePath)
    }

    def loadPlayerScores(filePath: String) = {
        file.loadPlayerScores(filePath)
    }

    def showVisibleCell(x: Int, y: Int): String = field.showVisibleCell(x,y).toString

    def getFieldSize: Int = field.matrix.size
    def getSpielbrettState: String = game.board
    def getControllerField: IField = field
    def getControllerGame: IGame = game
    
    def put(move: Move): IField = undoRedoManager.doStep(field, DoCommand(move))
    def undo: IField = undoRedoManager.undoStep(field)
    def redo: IField = undoRedoManager.redoStep(field)

    override def toString = field.toString

    //def checkExit = if this.spielbrettState == "Lost" || this.spielbrettState == "Won" then true else false