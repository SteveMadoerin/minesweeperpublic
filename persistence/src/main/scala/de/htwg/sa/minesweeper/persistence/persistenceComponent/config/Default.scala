package de.htwg.sa.minesweeper.persistence.persistenceComponent.config

import de.htwg.sa.minesweeper.persistence.entity._
import de.htwg.sa.minesweeper.persistence.persistenceComponent._
/* import de.htwg.sa.minesweeper.model.gameComponent.gameBaseImpl._
import de.htwg.sa.minesweeper.model.gameComponent._ */
import de.htwg.sa.minesweeper.persistence.persistenceComponent.IPersistence
import de.htwg.sa.minesweeper.persistence.persistenceComponent.persistenceJsonImpl.Persistence as JsonFileIO
import de.htwg.sa.minesweeper.persistence.persistenceComponent.persistenceXmlImpl.Persistence as XmlFileIO


object Default{
    
    def scalableMatrix(size: Int, filling: String): Matrix[String] = new Matrix(size, filling)
    def scalableField(size: Int, filling: String): IField = new Field(size, filling)
    def mergeMatrixToField(sichtbar: Matrix[String], unsichtbar: Matrix[String] ): IField = new Field(sichtbar, unsichtbar)

    given IPersistence = new JsonFileIO() //choose Implementation here
    val filePathHighScore = "C:\\Playground\\minesweeperpublic\\src\\main\\data\\highscore.json"
/*     given IFileIO = new XmlFileIO() //choose Implementation here
    val filePathHighScore = ""C:\\Playground\\minesweeperpublic\\src\\main\\data\\highscore.xml" */
    
}
