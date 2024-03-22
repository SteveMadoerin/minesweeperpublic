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
        if(this.hidden.cell(y, x) == Symbols.Bomb){spiel.handleGameState("Lost")} else if(spiel.calcWonOrLost(this.matrix, spiel.getBombs)){spiel.handleGameState("Won")}
        val extractedSymbol = this.showInvisibleCell(y, x)
        val returnField = this.put(extractedSymbol, y, x)
        returnField
    
    def gameOverField: IField = new Field(this.getHidden)
    
    def reveal =
        val revMat = new Field(this.hidden)
        println(revMat.toString())
        revMat

    def put(symbol: Symbols, x: Int, y: Int) = copy(matrix.replaceCell(x, y, symbol))
    //def get(x: Int, y: Int): Symbols = matrix.cell(x, y)
    def showVisibleCell(x: Int, y: Int): Symbols = matrix.cell(x, y)
    def showInvisibleCell(x: Int, y: Int): Symbols = hidden.cell(x, y)
    def isValidF(row: Int, col: Int, side: Int): Boolean = {row >= 0 && row <= side && col >= 0 && col <= side}

    def openNewXXX(x: Int, y: Int, field: IField): IField =
        val extractedSymbol = field.showInvisibleCell(y, x)
        val returnField = field.put(extractedSymbol, y, x)
        returnField

    def recursiveMadness(x: Int, y: Int, field: IField): IField = {
        var mysteriousField = field

        val directions = List(
            (-1, -1), (-1, 0), (-1, 1),
            ( 0, -1),          ( 0, 1),
            ( 1, -1), ( 1, 0), ( 1, 1)
        )

        for ((dx, dy) <- directions) {
            val newX = x + dx
            val newY = y + dy

            if (isValidF(newY, newX, field.getFieldSize - 1)) {
                val currentCell = field.showVisibleCell(newY, newX)
                val invisibleCell = field.showInvisibleCell(newY, newX)

                if (currentCell == Symbols.Covered && invisibleCell == Symbols.Zero) {
                    mysteriousField = openNewXXX(newX, newY, mysteriousField)
                    mysteriousField = recursiveMadness(newX, newY, mysteriousField)
                }

                if (currentCell == Symbols.Covered && invisibleCell != Symbols.Zero) {
                    mysteriousField = openNewXXX(newX, newY, mysteriousField)
                }
            }
        }

        mysteriousField
    }

    //def getField = this
    def getFieldSize: Int = size
    def getMatrix: Matrix[Symbols] = matrix
    def getHidden: Matrix[Symbols] = hidden
    
    //def setInvisibleMatrix(matrix: Matrix[Symbols]): Unit =  copy(hidden = matrix) //matrix
    //def setVisibleMatrix(matrix: Matrix[Symbols]): Unit = copy(matrix = matrix)
    override def toString(): String = mesh()
