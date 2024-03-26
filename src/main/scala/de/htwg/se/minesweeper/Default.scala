package de.htwg.se.minesweeper

import de.htwg.se.minesweeper.controller.controllerComponent.IController
import de.htwg.se.minesweeper.controller.controllerComponent.controllerBaseImpl.Controller
import de.htwg.se.minesweeper.model.gameComponent.gameBaseImpl._
import de.htwg.se.minesweeper.model.gameComponent._
import de.htwg.se.minesweeper.model.fileIoComponent.IFileIO
import de.htwg.se.minesweeper.model.fileIoComponent.fileIoXmlImpl.{FileIO => XmlFileIO}
import de.htwg.se.minesweeper.model.fileIoComponent.fileIoJsonImpl.{FileIO => JsonFileIO}


object Default{
    // NEW
    given IGame = prepareGame
    given IField = prepareGame._field
    // NEW
    given IController = Controller()
    
/*     given IFileIO = new JsonFileIO() //choose Implementation here
    val filePathHighScore = "C:\\github\\scalacticPlayground\\minesweeper\\src\\main\\data\\highscore.json" */
    given IFileIO = new XmlFileIO() //choose Implementation here
    val filePathHighScore = "C:\\github\\scalacticPlayground\\minesweeper\\src\\main\\data\\highscore.xml"

    def scalableMatrix(size: Int, filling: Symbols): Matrix[Symbols] = new Matrix(size, filling)
    def scalableField(size: Int, filling: Symbols): IField = new Field(size, filling)
    def mergeMatrixToField(sichtbar: Matrix[Symbols], unsichtbar: Matrix[Symbols] ): IField = new Field(sichtbar, unsichtbar)

    def prepareGame = {
        var realGame = new Game(10 , 9)
        realGame.board = "Playing"
        realGame.createField
        realGame
    }

    def prepareGame(bombs: Int, size: Int, time : Int) : IGame = {
        var realGame = new Game(bombs, size)
        realGame.board = "Playing"
        realGame.time = Vector(time)
        realGame.createField
        realGame
        
    }

}