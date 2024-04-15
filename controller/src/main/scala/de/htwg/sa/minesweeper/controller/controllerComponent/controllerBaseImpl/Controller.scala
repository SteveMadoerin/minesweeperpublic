package de.htwg.sa.minesweeper.controller.controllerComponent.controllerBaseImpl

import de.htwg.sa.minesweeper.controller.controllerComponent.config.Default
import de.htwg.sa.minesweeper.controller.controllerComponent.IController
import de.htwg.sa.minesweeper.model.gameComponent.gameBaseImpl._
import de.htwg.sa.minesweeper.model.gameComponent._
import de.htwg.sa.minesweeper.persistence.fileIoComponent.IFileIO
import de.htwg.sa.minesweeper.shared.{Observable, Move, UndoRedoManager, Event}


class Controller(using var game: IGame, var file: IFileIO) extends IController with Observable:
    
    var field: IField = Default.createField(game)
    val decider = new Decider()
    val undoRedoManager = new UndoRedoManager[IField]
    
    def doMove(b: Boolean, move: Move, game: IGame) = 
        val (tempGame, tempField): (IGame, IField) = decider.evaluateStrategy(b, move.x, move.y, field, game)
        field = tempField 
        this.game = tempGame
        if(field.showInvisibleCell(move.y, move.x) == Symbols.Zero){field}
        else{undoRedoManager.doStep(field, DoCommand(move))}

    def loadGame =
        // TWO TRACK CODE
        val gameBox = file.loadGame
        val gameBoxList = List.fill(1)(gameBox)
        val packGame = new PackGame(gameBoxList)
        
        val extractedGameList = for (
            game <- packGame.games
        ) yield game.game match {
            case Some(g) => g
            case None =>  this.game
        }

        val extractedGame = extractedGameList(0)

        game = copyInterface(extractedGame, "Playing")

        val fieldOption = file.loadField
        fieldOption match {
            case Some(field) => this.field = field
            case None =>
        }

        notifyObservers(Event.Load)
    
    def saveGame =

        notifyObservers(Event.SaveTime)
        file.saveGame(game)
        file.saveField(field)

        game = copyInterface(game, "Playing")
        notifyObservers(Event.Save)
    
    def exit = notifyObservers(Event.Exit)

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
    
    def checkGameOver(status: String) = game.checkExit(status)

    def newGameGUI =
        game = copyInterface(game, "Playing")

        notifyObservers(Event.Input)
    
    //for GUI
    def newGameField(optionString: Option[String]) =
        game = copyInterface(game, "Playing")
        val prepareWithDifficulty = game.prepareBoard(optionString)_ // partially applied and get a function
        val (feld, spiel): (IField, IGame) = prepareWithDifficulty(game)// complete preparation with game instance
        field = feld
        game = spiel
        
        notifyObservers(Event.NewGame)
        
    def newGame(side: Int, bombs: Int) =
        val tempGame: Game = game.asInstanceOf[Game]
        val gameCopy = tempGame.copy(bombs, side, tempGame.time, "Playing")
        val newGame: IGame = gameCopy
        game = newGame
        field = Default.createField(game)
        notifyObservers(Event.NewGame)
    
    // doMove wird von TUI als parameter übergeben hierzu wird game und field in der TUI Klasse angegeben
    def makeAndPublish(makeThis: (Boolean, Move, IGame) => IField, b: Boolean, move: Move, game: IGame): Unit =
        field = makeThis(b, move, game)  // doMove
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
    
    def put(move: Move): IField = undoRedoManager.doStep(field, DoCommand(move))
    def undo: IField = undoRedoManager.undoStep(field)
    def redo: IField = undoRedoManager.redoStep(field)

    override def toString = field.toString

    def saveTime(currentTime: Int) = 
        val tempGame: Game = game.asInstanceOf[Game]
        val gameCopy = tempGame.copy(time = currentTime)
        val newGame: IGame = gameCopy
        game = gameCopy
    
    // maybe we can make a Util class and put it in there
    def copyInterface(spiel: IGame, status: String) = 
        val tempGame: Game = spiel.asInstanceOf[Game]
        val gameCopy = tempGame.copy(tempGame.bombs, tempGame.side, tempGame.time, status)
        val newGame: IGame = gameCopy
        newGame

    
end Controller