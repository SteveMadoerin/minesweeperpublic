package de.htwg.sa.minesweeper

import controller.controllerComponent.IController
import controller.controllerComponent.controllerBaseImpl.Controller
import de.htwg.sa.minesweeper.model.gameComponent.gameBaseImpl._
import de.htwg.sa.minesweeper.model.gameComponent._
//import de.htwg.sa.minesweeper.util.{Observable, Move, UndoRedoManager, Event}
import de.htwg.sa.minesweeper.persistence.persistenceComponent.IPersistence
import de.htwg.sa.minesweeper.persistence.persistenceComponent.persistenceJsonImpl.Persistence as JsonFileIO
import de.htwg.sa.minesweeper.persistence.persistenceComponent.persistenceXmlImpl.Persistence as XmlFileIO


object Default{
    
    given IGame = Game(10, 9, 0, "Playing")
    given IField = createField(prepareGame(10, 9, 0))
    given IController = Controller()
    given IPersistence = new JsonFileIO() //choose Implementation here
    val filePathHighScore = "C:\\Playground\\minesweeperpublic\\src\\main\\data\\highscore.json"
/*     given IFileIO = new XmlFileIO() //choose Implementation here
    val filePathHighScore = "C:\\Playground\\minesweeperpublic\\src\\main\\data\highscore.xml" */

    def scalableMatrix(size: Int, filling: String): Matrix[String] = new Matrix(size, filling)
    def scalableField(size: Int, filling: String): IField = new Field(size, filling)
    def mergeMatrixToField(sichtbar: Matrix[String], unsichtbar: Matrix[String] ): IField = Field(sichtbar, unsichtbar)

    def prepareGame(bombs: Int, size: Int, time : Int) : IGame =  Game(bombs, size, time, "Playing")

    def createField(leGame: IGame): IField = {
        val adjacentField = Playfield()
        val tempGame: Game = leGame.asInstanceOf[Game]
        adjacentField.newField(leGame.side, tempGame)
    }
    
}