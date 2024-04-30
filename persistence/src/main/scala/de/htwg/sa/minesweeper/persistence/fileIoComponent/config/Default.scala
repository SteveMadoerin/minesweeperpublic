package de.htwg.sa.minesweeper.persistence.fileIoComponent.config

import de.htwg.sa.minesweeper.model.gameComponent.gameBaseImpl._
import de.htwg.sa.minesweeper.model.gameComponent._


object Default{
    
    def scalableMatrix(size: Int, filling: String): Matrix[String] = new Matrix(size, filling)
    def scalableField(size: Int, filling: String): IField = new Field(size, filling)
    def mergeMatrixToField(sichtbar: Matrix[String], unsichtbar: Matrix[String] ): IField = new Field(sichtbar, unsichtbar)
    
}
