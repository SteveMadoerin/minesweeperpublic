package de.htwg.sa.minesweeper.model.gameComponent.gameBaseImpl

import de.htwg.sa.minesweeper.model.gameComponent._
import scala.io.StdIn.readLine
import scala.util.{Random, Try}
import scala.annotation.tailrec
import de.htwg.sa.minesweeper.model.gameComponent.config.Default
/* import de.htwg.sa.minesweeper.Default
import de.htwg.sa.minesweeper.Default.given */

case class Game (bombs : Int, side: Int, time: Int, board : String) extends IGame:
    
    def insertBomb(newBombs: Int): Game = copy(bombs = newBombs)
    def insertSide(newSide: Int): Game = copy(side = newSide)
    def insertTime(newTime: Int): Game = copy(time = newTime)
    def insertBoard(newBoard: String): Game = copy(board = newBoard)
    

    def optionToList(s: Option[String]): List[Int] = {
        s match{
            case Some("SuperEasy") => List(5, 5)
            case Some("Easy") => List(9, 10)
            case Some("Medium") => List(15, 40)
            case Some("Hard") => List(19, 85)
            case _ => List(9, 10)
        }
    }

    // currying
    def prepareBoard(s: Option[String])(game: IGame): (IField, IGame) = {
        val realGame = this.copy(optionToList(s)(1), optionToList(s)(0))
        val adjacentField = Playfield()
        (adjacentField.newField(optionToList(s)(0), realGame), realGame)
    }

    def helpMessage = 
        val helpMsg = 
            """******************************************************************************
            |* This is Minesweeper HELP - MENU                                            *
            |* Enter your move (<action><x><y>, eg. o0002, q to quit, h for help):        *
            |*                                                                            *
            |* Possible moves: o0003 ----> opens cell on coordinates x=0, y=3             *
            |*                 f0202 ----> flags cell on coordinates x=2, y=2             *
            |*                 u0202 ----> removes flag at cell on coordinates x=2, y=2   *
            |*                 z   ----> undo Move                                        *
            |*                 y   ----> redo Move                                        *
            |*                 r   ----> reaveals field                                   *
            |*                 h   ----> help                                             *
            |*                 q   ----> quits game                                       *
            |*                                                                            *
            |* Copyright: Steve Madoerin                                                  *
            |*                                                                            *
            |******************************************************************************""".stripMargin
            
        println(s"$helpMsg")

    
    def premierMove(x: Int, y: Int, field: IField): IField = 
        val adjacent = Playfield()
        val adjacentInitialised = adjacent.newField(side, this)
        val newReplacedbombHidMatrix = if(adjacentInitialised.hidden.cell(y, x) == Symbols.Bomb){
            val replacedHiddenMatrix = replaceBomb(x, y, adjacentInitialised)
            val replacedAdjacentHiddenField = Default.mergeMatrixToField(field.matrix, replacedHiddenMatrix.hidden)
            replacedAdjacentHiddenField
        } else {
            adjacentInitialised
        }
        val newVisibleMatrix = newReplacedbombHidMatrix.openNew(x, y, newReplacedbombHidMatrix)
        newVisibleMatrix
    
    def replaceBomb(x: Int, y: Int, field: IField): IField = {
        val size = field.matrix.size
        val indices = 0 until size
        val result = indices
            .flatMap(col => indices.map(row => (row, col)))
            .filter { case (row, col) => field.hidden.cell(row, col) != Symbols.Bomb }

        val rand = Random.nextInt(result.size)
        val (newX, newY) = result(rand)

        val newHiddenMatrixWithBomb = field.hidden.replaceCell(newY, newX, Symbols.Bomb)
        val newHiddenMatrixWithoutBomb = newHiddenMatrixWithBomb.replaceCell(y, x, Symbols.Empty)
        val newHiddenMatrixWithNumbers = initializeAdjacentNumbers(newHiddenMatrixWithoutBomb)

        val newVisibleMatrix = field.matrix.replaceCell(y, x, Symbols.Covered)

        field match {
            case f: Field => f.copy(
                hidden = newHiddenMatrixWithNumbers,
                matrix = newVisibleMatrix
            )
            case _ => field 
        }
    }
    
    // currying
    def calcX(symbols:Symbols)(visibleMatrix: Matrix[Symbols]): Int = {
        val sizze = visibleMatrix.size -1
        val multiIndex = 0 to sizze
        multiIndex
          .flatMap(row => multiIndex.map(col => (row, col)))
          .count{ case(row, col) => visibleMatrix.cell(row, col) == symbols && isValid(row, col, sizze)}
    }

    def calcCovered (visibleMatrix: Matrix[Symbols]): Int = calcX(Symbols.Covered)(visibleMatrix)
    def calcFlag (visibleMatrix: Matrix[Symbols]): Int = calcX(Symbols.F)(visibleMatrix)

    def calcMineAndFlag(visibleMatrix: Matrix[Symbols]): Int = (this.bombs - calcFlag(visibleMatrix))

    def calcAdjacentMines(row: Int, col: Int, side: Int, invisibleMatrix: Matrix[Symbols]): Int = {

        val neighbors = List(
            (row - 1, col), // NORTH
            (row + 1, col), // SOUTH
            (row, col + 1), // EAST
            (row, col - 1), // WEST
            (row - 1, col + 1), // NORTH-EAST
            (row - 1, col - 1), // NORTH-WEST
            (row + 1, col + 1), // SOUTH-EAST
            (row + 1, col - 1) // SOUTH-WEST
        )
        neighbors.count { case (r, c) => isValid(r, c, side) && isMine(r, c, invisibleMatrix) }
    }

    def calcWonOrLost(visibleMatrix: Matrix[Symbols], mines: Int): Boolean = (mines+1 - (calcFlag(visibleMatrix) + calcCovered(visibleMatrix)) == 0)
    
    def isMine(row: Int, col: Int, m: Matrix[Symbols]): Boolean = {if(m.cell(row, col) == Symbols.Bomb) true else false} // fc
    def isValid(row: Int, col: Int, side: Int): Boolean = {row >= 0 && row <= side && col >= 0 && col <= side} // fc

    def initializeAdjacentNumbers(matrix: Matrix[Symbols]): Matrix[Symbols] =
        val si = matrix.size - 1
        val multiIndex = 0 to si
        multiIndex
            .flatMap(col => multiIndex.map(row => (row, col))) // .flatMap and .map to create collection of all possible (row, col) pairs
            .foldLeft(matrix) { // .foldLeft to iterate over each pair & update Matrix
                (matr, pos) => val (row, col) = pos
                if(matr.cell(row, col) != Symbols.Bomb && isValid(row, col, si)) {
                    val numero = calcAdjacentMines(row, col, si, matrix)
                    val symb = numero match{
                        case 0 => Symbols.Zero
                        case 1 => Symbols.One
                        case 2 => Symbols.Two
                        case 3 => Symbols.Three
                        case 4 => Symbols.Four
                        case 5 => Symbols.Five
                        case 6 => Symbols.Six
                        case 7 => Symbols.Seven
                        case 8 => Symbols.Eight
                    }
                    matr.replaceCell(row, col, symb)
                } else matr
            }
    
    def intitializeBombs(matrix: Matrix[Symbols], bombs: Int): Matrix[Symbols] = {
        val sizze = matrix.size - 1
        val random = new Random()

        // recursion
        @tailrec
        def placeMines(ma: Matrix[Symbols], minesPlaced: Int): Matrix[Symbols] = {
            if (minesPlaced >= bombs) ma
            else {
                val row = random.nextInt(sizze)
                val col = random.nextInt(sizze)

                if (ma.cell(row, col) != Symbols.Bomb) {
                    val updatedMatrix = ma.replaceCell(row, col, Symbols.Bomb)
                    placeMines(updatedMatrix, minesPlaced + 1) // recursion
                } else {
                    placeMines(ma, minesPlaced) // recursion
                }
            }
        }
        placeMines(matrix, 0)
    }
    
    def checkExit(status: String) = if status == "Lost" || status == "Won" then true else false


end Game
