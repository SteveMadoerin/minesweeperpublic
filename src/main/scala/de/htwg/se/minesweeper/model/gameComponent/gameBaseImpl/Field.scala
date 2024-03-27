package de.htwg.se.minesweeper.model.gameComponent.gameBaseImpl

import de.htwg.se.minesweeper.model.gameComponent._

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

    def open(x: Int, y: Int, spiel: IGame): IField =
        if(this.hidden.cell(y, x) == Symbols.Bomb){spiel.handleGameState("Lost")} else if(spiel.calcWonOrLost(this.matrix, spiel._bombs)){spiel.handleGameState("Won")}
        val extractedSymbol = this.showInvisibleCell(y, x)
        val returnField = this.put(extractedSymbol, y, x)
        returnField
    
    def gameOverField: IField = new Field(this._hidden)
    
    def reveal =
        val revMat = new Field(this.hidden)
        println(revMat.toString())
        revMat

    def put(symbol: Symbols, x: Int, y: Int) = copy(matrix.replaceCell(x, y, symbol))
    def showVisibleCell(x: Int, y: Int): Symbols = matrix.cell(x, y)
    def showInvisibleCell(x: Int, y: Int): Symbols = hidden.cell(x, y)
    def isValidF(row: Int, col: Int, side: Int): Boolean = {row >= 0 && row <= side && col >= 0 && col <= side}
    
    //def _matrix: Matrix[Symbols] = matrix
    def _hidden: Matrix[Symbols] = hidden

    def openNewXXX(x: Int, y: Int, field: IField): IField =
        val extractedSymbol = field.showInvisibleCell(y, x)
        val returnField = field.put(extractedSymbol, y, x)
        returnField

    def recursiveMadness(x: Int, y: Int, field: IField): IField = {
        val directions = List(
            (-1, -1), (-1, 0), (-1, 1),
            (0, -1),          (0, 1),
            (1, -1), (1, 0), (1, 1)
        )
    
        def exploreDirections(currentField: IField, remainingDirections: List[(Int, Int)]): IField = remainingDirections match {
            case (dx, dy) :: tail =>
            val newX = x + dx
            val newY = y + dy
            if (isValidF(newY, newX, currentField.matrix.size - 1)) {
                val currentCell = currentField.showVisibleCell(newY, newX)
                val invisibleCell = currentField.showInvisibleCell(newY, newX)
                val updatedField = if (currentCell == Symbols.Covered) {
                val openedField = openNewXXX(newX, newY, currentField)
                if (invisibleCell == Symbols.Zero) recursiveMadness(newX, newY, openedField)
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
