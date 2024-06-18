package de.htwg.sa.minesweeper.model.gameComponent.gameBaseImpl

import de.htwg.sa.minesweeper.model.gameComponent._


abstract class OpenStrategy{
    def show(x: Int, y: Int, field: IField, game: IGame) =
        showField(x, y, field, game)

    def showField(x: Int, y: Int, field: IField, game: IGame): (IGame, IField)
}

class FirstMove extends OpenStrategy{
    override def showField(x: Int, y: Int, field: IField, game: IGame): (IGame, IField) = (game, game.premierMove(x, y, field))
}

class NormalMove extends OpenStrategy{
    override def showField(x: Int, y: Int, field: IField, game: IGame): (IGame, IField) = field.open(x, y, game)
}