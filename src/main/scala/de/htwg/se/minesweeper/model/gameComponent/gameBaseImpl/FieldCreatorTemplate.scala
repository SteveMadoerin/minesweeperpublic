package de.htwg.se.minesweeper.model.gameComponent.gameBaseImpl

import de.htwg.se.minesweeper.model.gameComponent._
import scala.util.Random
import de.htwg.se.minesweeper.Default


abstract class FieldCreatorTemplate{

    def newField(side: Int, spiel: Game): IField = 
        val emptyField = createEmptyField(side)
        val initialisedField = initialiseInvisibleMatrix(emptyField, spiel)
        initialisedField

    protected def createEmptyField(side: Int ): IField = Default.scalableField(side, Symbols.Covered) // Dependency Injection


    protected def initialiseInvisibleMatrix(field: IField, game: Game): IField = field
}

class Minefield extends FieldCreatorTemplate{
    override protected def createEmptyField(side: Int): IField = 
        super.createEmptyField(side)
    
    override protected def initialiseInvisibleMatrix(field: IField, game: Game): IField = 

        val bombs = game.bombs
        val side = game.side
        val sichtbareMatrix = field.matrix
        val unsichtbareMatrix = Default.scalableMatrix(side, Symbols.Empty) // Dependency Injection
        val newUnsichtbareMatrix = game.intitializeBombs(unsichtbareMatrix, bombs)
        val newField = Default.mergeMatrixToField(sichtbareMatrix, newUnsichtbareMatrix) // Dependency Injection
        newField

}

class Playfield extends FieldCreatorTemplate{
    override protected def createEmptyField(side: Int): IField = super.createEmptyField(side)

    override protected def initialiseInvisibleMatrix(field: IField, game: Game): IField = 

        val bombs = game.bombs
        val side = game.side
        val sichtbareMatrix = field.matrix
        var unsichtbareMatrix = Default.scalableMatrix(side, Symbols.Empty) // Dependency Injection
        var newUnsichtbareMatrix = game.intitializeBombs(unsichtbareMatrix, bombs)
        val newUnsichtbareMatrixAdjacent = game.initializeAdjacentNumbers(newUnsichtbareMatrix)
        val newField = Default.mergeMatrixToField(sichtbareMatrix, newUnsichtbareMatrixAdjacent) // Dependency Injection
        newField

}

class EmptyField extends FieldCreatorTemplate{
    override protected def createEmptyField(side: Int): IField = super.createEmptyField(side)

    override protected def initialiseInvisibleMatrix(field: IField, game: Game): IField = super.initialiseInvisibleMatrix(field, game)
}