package de.htwg.sa.minesweeper.model.gameComponent.gameBaseImpl

import de.htwg.sa.minesweeper.model.gameComponent._
import util.chaining.scalaUtilChainingOps

case class Field(matrix: Matrix[Symbols], hidden: Matrix[Symbols]) extends IField:
    val size = matrix.size
    val endl = sys.props("line.separator")

    def this(size: Int, filling: Symbols)= this(new Matrix(size, filling), new Matrix(size, Symbols.Empty))
    def this(matrix: Matrix[Symbols])= this(matrix, matrix)

    def bar(cellWidth: Int = 3, cellNum: Int = 3) = (("+" + "-" * cellWidth) * cellNum) + "+" + endl
    def cells(row: Int = 3, cellWidth: Int = 3) = matrix.row(row).map(_.toString).map(" " * ((cellWidth-1)/2) + _ + " " *((cellWidth -1)/2)).mkString("|","|","|") + endl
    def mesh(cellWidth: Int = 3) = (0 until size).map(cells(_, cellWidth)).mkString(bar(cellWidth, size), bar(cellWidth, size), bar(cellWidth, size))
    
    def putFlag(x: Int, y: Int) = copy(matrix.replaceCell(y, x, Symbols.F))
    def removeFlag(x: Int, y: Int) = copy(matrix.replaceCell(y, x, Symbols.Covered))

    def open(x: Int, y: Int, spiel: IGame): (IGame, IField) =
        val currentGame: Game = spiel.asInstanceOf[Game]
        val hiddenCell = this.hidden.cell(y, x)

        val gameStatus = hiddenCell match {
            case Symbols.Bomb =>
                currentGame.copy(board = "Lost")
            case _ if spiel.calcWonOrLost(this.matrix, currentGame.bombs) =>
                currentGame.copy(board = "Won")
            case _ =>
                spiel
        }

        val extractedSymbol = this.showInvisibleCell(y, x)
        val returnField = this.put(extractedSymbol, y, x)
        (gameStatus, returnField)
    
    def gameOverField: IField = new Field(this.hidden)
    
    def reveal = new Field(this.hidden).tap(y => println(y.toString())) // function chaining

    def put(symbol: Symbols, x: Int, y: Int) = copy(matrix.replaceCell(x, y, symbol))
    def showVisibleCell(x: Int, y: Int): Symbols = matrix.cell(x, y)
    def showInvisibleCell(x: Int, y: Int): Symbols = hidden.cell(x, y)
    def isValidF(row: Int, col: Int, side: Int): Boolean = {row >= 0 && row <= side && col >= 0 && col <= side}
    def openNew(x: Int, y: Int, field: IField): IField = field.put(field.showInvisibleCell(y, x), y, x) // function chaining
    
    // recursion
    def recursiveOpen(x: Int, y: Int, field: IField): IField = {
        val directions = List(
            (-1, -1), (-1, 0), (-1, 1),
            (0, -1),          (0, 1),
            (1, -1), (1, 0), (1, 1)
        )

        // recursive function
        def exploreDirections(currentField: IField, remainingDirections: List[(Int, Int)]): IField = remainingDirections match {
            case (dx, dy) :: tail =>
            val newX = x + dx
            val newY = y + dy
            if (isValidF(newY, newX, currentField.matrix.size - 1)) {
                val currentCell = currentField.showVisibleCell(newY, newX)
                val invisibleCell = currentField.showInvisibleCell(newY, newX)
                val updatedField = if (currentCell == Symbols.Covered) {
                val openedField = openNew(newX, newY, currentField)
                if (invisibleCell == Symbols.Zero) recursiveOpen(newX, newY, openedField)
                else openedField
                } else currentField
                exploreDirections(updatedField, tail)
            } else {
                exploreDirections(currentField, tail)
            }
            case Nil => currentField
        }

        exploreDirections(field, directions)
    }

    override def toString(): String = mesh()
