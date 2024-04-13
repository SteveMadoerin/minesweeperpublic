package fileIoComponent.config

import model.gameComponent.gameBaseImpl._
import model.gameComponent._
import de.htwg.sa.minesweeper.util.{Observable, Move, UndoRedoManager, Event}


object Default{
    
    def scalableMatrix(size: Int, filling: Symbols): Matrix[Symbols] = new Matrix(size, filling)
    def scalableField(size: Int, filling: Symbols): IField = new Field(size, filling)
    def mergeMatrixToField(sichtbar: Matrix[Symbols], unsichtbar: Matrix[Symbols] ): IField = new Field(sichtbar, unsichtbar)
    
}
