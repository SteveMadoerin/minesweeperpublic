package de.htwg.sa.minesweeper.persistence.persistenceComponent.config

import de.htwg.sa.minesweeper.persistence.entity._
import de.htwg.sa.minesweeper.persistence.persistenceComponent._
import de.htwg.sa.minesweeper.persistence.persistenceComponent.IPersistence
import de.htwg.sa.minesweeper.persistence.persistenceComponent.persistenceJsonImpl.Persistence as JsonFileIO
import de.htwg.sa.minesweeper.persistence.persistenceComponent.persistenceXmlImpl.Persistence as XmlFileIO
import de.htwg.sa.minesweeper.persistence.persistenceComponent.persistenceSlickImpl.Persistence as SlickFileIO
import de.htwg.sa.minesweeper.persistence.persistenceComponent.persistenceMongoImpl.Persistence as MongoFileIO

import scala.io.StdIn.readLine


object Default{
    
    def scalableMatrix(size: Int, filling: String): Matrix[String] = new Matrix(size, filling)
    def scalableField(size: Int, filling: String): IField = new Field(size, filling)
    def mergeMatrixToField(sichtbar: Matrix[String], unsichtbar: Matrix[String] ): IField = new Field(sichtbar, unsichtbar)

    given IPersistence = new MongoFileIO
    //given IPersistence = choose
    private def choose: IPersistence =
        println("Choose Persistence: 1. JsonFileIO, 2. XmlFileIO, 3. SlickFileIO, 4. MongoFileIO: ")
        readLine().toInt match {
        case 1 => new JsonFileIO()
        case 2 => filePathHighScore = "C:\\Playground\\minesweeperpublic\\src\\main\\data\\highscore.xml"; XmlFileIO()
        case 3 => new SlickFileIO()
        case 4 => new MongoFileIO()
    }

    var filePathHighScore = "C:\\Playground\\minesweeperpublic\\src\\main\\data\\highscore.json"
    //given IPersistence = new JsonFileIO() //choose Implementation here
    //given IFileIO = new XmlFileIO() //choose Implementation here
    //val filePathHighScore = "C:\\Playground\\minesweeperpublic\\src\\main\\data\\highscore.xml"
    
}
