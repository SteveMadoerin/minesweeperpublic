package aview.config

import controller.controllerComponent.IController
import controller.controllerComponent.controllerBaseImpl.Controller
import model.gameComponent.gameBaseImpl._
import model.gameComponent._
import de.htwg.sa.minesweeper.util.{Observable, Move, UndoRedoManager, Event}

import fileIoComponent.IFileIO
import fileIoComponent.fileIoXmlImpl.{FileIO => XmlFileIO}
import fileIoComponent.fileIoJsonImpl.{FileIO => JsonFileIO}



object Default{
    
    /*given IFileIO = new JsonFileIO() //choose Implementation here
    val filePathHighScore = "C:\\github\\scalacticPlayground\\minesweeper\\src\\main\\data\\highscore.json"*/
    given IFileIO = new XmlFileIO() //choose Implementation here
    val filePathHighScore = "C:\\github\\scalacticPlayground\\minesweeper\\src\\main\\data\\highscore.xml"
}