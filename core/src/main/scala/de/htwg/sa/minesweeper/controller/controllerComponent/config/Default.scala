package de.htwg.sa.minesweeper.controller.controllerComponent.config

import model.gameComponent.gameBaseImpl._
import model.gameComponent._
import de.htwg.sa.minesweeper.util.{Observable, Move, UndoRedoManager, Event}


object Default{
    
    given IField = createField(prepareGame(10, 9, 0))

    def prepareGame(bombs: Int, size: Int, time : Int) : IGame =  Game(bombs, size, time, "Playing")

    def createField(leGame: IGame): IField = {
        val adjacentField = Playfield()
        val tempGame: Game = leGame.asInstanceOf[Game]
        adjacentField.newField(leGame.side, tempGame)
    }
    
}
