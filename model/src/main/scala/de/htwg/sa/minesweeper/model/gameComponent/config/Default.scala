package de.htwg.sa.minesweeper.model.gameComponent.config

import de.htwg.sa.minesweeper.model.gameComponent._
import de.htwg.sa.minesweeper.model.gameComponent.gameBaseImpl._

object Default {

    given IGame = Game(10, 9, 0, "Playing")
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
}
