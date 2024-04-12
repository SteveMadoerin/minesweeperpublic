package model.gameComponent.gameBaseImpl

import model.gameComponent._
import scala.util.Random
/* import de.htwg.sa.minesweeper.Default */


abstract class FieldCreatorTemplate{

    def newField(side: Int, spiel: Game): IField = 
        val emptyField = createEmptyField(side)
        val initialisedField = initialiseInvisibleMatrix(emptyField, spiel)
        initialisedField

    protected def createEmptyField(side: Int ): IField = 
        def scalableField(size: Int, filling: Symbols): IField = new Field(size, filling)
        scalableField(side, Symbols.Covered)


    protected def initialiseInvisibleMatrix(field: IField, game: Game): IField = field
}

class Minefield extends FieldCreatorTemplate{
    override protected def createEmptyField(side: Int): IField = 
        super.createEmptyField(side)
    
    override protected def initialiseInvisibleMatrix(field: IField, game: Game): IField =

        def scalableMatrix(size: Int, filling: Symbols): Matrix[Symbols] = new Matrix(size, filling)
        def mergeMatrixToField(sichtbar: Matrix[Symbols], unsichtbar: Matrix[Symbols] ): IField = new Field(sichtbar, unsichtbar) 

        val bombs = game.bombs
        val side = game.side
        val sichtbareMatrix = field.matrix
        val unsichtbareMatrix = scalableMatrix(side, Symbols.Empty)
        val newUnsichtbareMatrix = game.intitializeBombs(unsichtbareMatrix, bombs)
        val newField = mergeMatrixToField(sichtbareMatrix, newUnsichtbareMatrix)
        newField

}

class Playfield extends FieldCreatorTemplate{
    override protected def createEmptyField(side: Int): IField = super.createEmptyField(side)

    override protected def initialiseInvisibleMatrix(field: IField, game: Game): IField = 

    
        def scalableMatrix(size: Int, filling: Symbols): Matrix[Symbols] = new Matrix(size, filling)
        def mergeMatrixToField(sichtbar: Matrix[Symbols], unsichtbar: Matrix[Symbols] ): IField = new Field(sichtbar, unsichtbar) 

        val bombs = game.bombs
        val side = game.side
        val sichtbareMatrix = field.matrix
        val unsichtbareMatrix = scalableMatrix(side, Symbols.Empty)
        val newUnsichtbareMatrix = game.intitializeBombs(unsichtbareMatrix, bombs)
        val newUnsichtbareMatrixAdjacent = game.initializeAdjacentNumbers(newUnsichtbareMatrix)
        val newField = mergeMatrixToField(sichtbareMatrix, newUnsichtbareMatrixAdjacent)
        newField

}

class EmptyField extends FieldCreatorTemplate{
    override protected def createEmptyField(side: Int): IField = super.createEmptyField(side)

    override protected def initialiseInvisibleMatrix(field: IField, game: Game): IField = super.initialiseInvisibleMatrix(field, game)
}