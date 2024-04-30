package de.htwg.sa.minesweeper.ui.config

import de.htwg.sa.minesweeper.controller.controllerComponent.IController
import de.htwg.sa.minesweeper.controller.controllerComponent.controllerBaseImpl.Controller
import de.htwg.sa.minesweeper.model.gameComponent.gameBaseImpl._
import de.htwg.sa.minesweeper.model.gameComponent._
import de.htwg.sa.minesweeper.util.{Observable, Move, UndoRedoManager, Event}

import de.htwg.sa.minesweeper.persistence.fileIoComponent.IFileIO
import de.htwg.sa.minesweeper.persistence.fileIoComponent.fileIoXmlImpl.{FileIO => XmlFileIO}
import de.htwg.sa.minesweeper.persistence.fileIoComponent.fileIoJsonImpl.{FileIO => JsonFileIO}



object Default{

    given IController = Controller()

    given IGame = prepareGame(10, 9, 0)
    given IField = createField(prepareGame(10, 9, 0))

    def scalableMatrix(size: Int, filling: String): Matrix[String] = new Matrix(size, filling)
    def scalableField(size: Int, filling: String): IField = new Field(size, filling)
    def mergeMatrixToField(sichtbar: Matrix[String], unsichtbar: Matrix[String] ): IField = new Field(sichtbar, unsichtbar)

    def prepareGame(bombs: Int, size: Int, time : Int) : IGame =  Game(bombs, size, time, "Playing")

    def createField(leGame: IGame): IField = {
        val adjacentField = Playfield()
        val tempGame: Game = leGame.asInstanceOf[Game]
        adjacentField.newField(leGame.side, tempGame)
    }
    
    given IFileIO = new JsonFileIO() //choose Implementation here
    val filePathHighScore = "C:\\Playground\\minesweeperpublic\\src\\main\\data\\highscore.json"
/*     given IFileIO = new XmlFileIO() //choose Implementation here
    val filePathHighScore = ""C:\\Playground\\minesweeperpublic\\src\\main\\data\\highscore.xml" */
}