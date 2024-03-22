package de.htwg.se.minesweeper.model.gameComponent.gameBaseImpl

import de.htwg.se.minesweeper.model.gameComponent._


abstract class OpenStrategy{
    def show(x: Int, y: Int, field: IField, game: IGame) =
        showField(x, y, field, game)

    def showField(x: Int, y: Int, field: IField, game: IGame): IField

}

class FirstMove extends OpenStrategy{
    override def showField(x: Int, y: Int, field: IField, game: IGame): IField = game.premierMove(x, y, field)

}

class NormalMove extends OpenStrategy{
    override def showField(x: Int, y: Int, field: IField, game: IGame): IField = 
        val feld = field.open(x, y, game)
        val elGame = game
        feld

}