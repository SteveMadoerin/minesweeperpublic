package de.htwg.se.minesweeper.model.gameComponent.gameBaseImpl

import de.htwg.se.minesweeper.model.gameComponent._


abstract class OpenStrategy{
    def show(x: Int, y: Int, field: IField, game: IGame) =
        getField(x, y, field, game)

    def getField(x: Int, y: Int, field: IField, game: IGame): IField //name umbennen

}

class FirstMove extends OpenStrategy{
    override def getField(x: Int, y: Int, field: IField, game: IGame): IField = game.premierMove(x, y, field) //umbennen

}

class NormalMove extends OpenStrategy{
    override def getField(x: Int, y: Int, field: IField, game: IGame): IField = //umbennen
        val feld = field.open(x, y, game)
        val elGame = game
        feld

}