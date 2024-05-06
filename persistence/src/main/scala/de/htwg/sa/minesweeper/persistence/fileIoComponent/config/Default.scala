package de.htwg.sa.minesweeper.persistence.fileIoComponent.config

import de.htwg.sa.minesweeper.persistence.entity._
/* import de.htwg.sa.minesweeper.model.gameComponent.gameBaseImpl._
import de.htwg.sa.minesweeper.model.gameComponent._ */
import de.htwg.sa.minesweeper.persistence.fileIoComponent.IFileIO
import de.htwg.sa.minesweeper.persistence.fileIoComponent.fileIoJsonImpl.{FileIO => JsonFileIO}
import de.htwg.sa.minesweeper.persistence.fileIoComponent.fileIoXmlImpl.{FileIO => XmlFileIO}


object Default{
    
    def scalableMatrix(size: Int, filling: String): Matrix[String] = new Matrix(size, filling)
    def scalableField(size: Int, filling: String): IField = new Field(size, filling)
    def mergeMatrixToField(sichtbar: Matrix[String], unsichtbar: Matrix[String] ): IField = new Field(sichtbar, unsichtbar)

    given IFileIO = new JsonFileIO() //choose Implementation here
    val filePathHighScore = "C:\\Playground\\minesweeperpublic\\src\\main\\data\\highscore.json"
/*     given IFileIO = new XmlFileIO() //choose Implementation here
    val filePathHighScore = ""C:\\Playground\\minesweeperpublic\\src\\main\\data\\highscore.xml" */
    
}
