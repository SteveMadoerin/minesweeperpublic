package model.gameComponent.gameBaseImpl

import model.gameComponent._
import scala.util.Random
import model.gameComponent.config.Default
//import de.htwg.sa.minesweeper.Default

abstract class FieldCreatorTemplate{

    def newField(side: Int, spiel: Game): IField = 
        val emptyField = createEmptyField(side)
        val initialisedField = initialiseInvisibleMatrix(emptyField, spiel)
        initialisedField

    protected def createEmptyField(side: Int ): IField = Default.scalableField(side, Symbols.Covered)

    protected def initialiseInvisibleMatrix(field: IField, game: Game): IField = field
}

class Minefield extends FieldCreatorTemplate{
    override protected def createEmptyField(side: Int): IField = 
        super.createEmptyField(side)
    
    override protected def initialiseInvisibleMatrix(field: IField, game: Game): IField =

        val bombs = game.bombs
        val side = game.side
        val sichtbareMatrix = field.matrix
        val unsichtbareMatrix = Default.scalableMatrix(side, Symbols.Empty)
        val newUnsichtbareMatrix = game.intitializeBombs(unsichtbareMatrix, bombs)
        val newField = Default.mergeMatrixToField(sichtbareMatrix, newUnsichtbareMatrix)
        newField

}

class Playfield extends FieldCreatorTemplate{
    override protected def createEmptyField(side: Int): IField = super.createEmptyField(side)

    override protected def initialiseInvisibleMatrix(field: IField, game: Game): IField = 

        val bombs = game.bombs
        val side = game.side
        val sichtbareMatrix = field.matrix
        val unsichtbareMatrix = Default.scalableMatrix(side, Symbols.Empty)
        val newUnsichtbareMatrix = game.intitializeBombs(unsichtbareMatrix, bombs)
        val newUnsichtbareMatrixAdjacent = game.initializeAdjacentNumbers(newUnsichtbareMatrix)
        val newField = Default.mergeMatrixToField(sichtbareMatrix, newUnsichtbareMatrixAdjacent)
        newField

}

class EmptyField extends FieldCreatorTemplate{
    override protected def createEmptyField(side: Int): IField = super.createEmptyField(side)

    override protected def initialiseInvisibleMatrix(field: IField, game: Game): IField = super.initialiseInvisibleMatrix(field, game)
}