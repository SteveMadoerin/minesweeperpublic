package de.htwg.sa.minesweeper

//import controller.controllerComponent.IController
//import controller.controllerComponent.controllerBaseImpl.Controller
import model.gameComponent.gameBaseImpl._
import model.gameComponent._
import de.htwg.sa.minesweeper.util.{Observable, Move, UndoRedoManager, Event}

import fileIoComponent.IFileIO
import fileIoComponent.fileIoXmlImpl.{FileIO => XmlFileIO}
import fileIoComponent.fileIoJsonImpl.{FileIO => JsonFileIO}


object Default{
    
    given IGame = prepareGame(10, 9, 0)
    given IField = createField(prepareGame(10, 9, 0))
    //given IController = Controller()
    
    /*given IFileIO = new JsonFileIO() //choose Implementation here
    val filePathHighScore = "C:\\github\\scalacticPlayground\\minesweeper\\src\\main\\data\\highscore.json"*/
    given IFileIO = new XmlFileIO() //choose Implementation here
    val filePathHighScore = "C:\\github\\scalacticPlayground\\minesweeper\\src\\main\\data\\highscore.xml"

    def scalableMatrix(size: Int, filling: Symbols): Matrix[Symbols] = new Matrix(size, filling)
    def scalableField(size: Int, filling: Symbols): IField = new Field(size, filling)
    def mergeMatrixToField(sichtbar: Matrix[Symbols], unsichtbar: Matrix[Symbols] ): IField = new Field(sichtbar, unsichtbar)

    def prepareGame(bombs: Int, size: Int, time : Int) : IGame =  Game(bombs, size, time, "Playing")

    def createField(leGame: IGame): IField = {
        val adjacentField = Playfield()
        val tempGame: Game = leGame.asInstanceOf[Game]
        adjacentField.newField(leGame.side, tempGame)
    }
    
}