package de.htwg.sa.minesweeper.model.gameComponent.gameBaseImpl

import de.htwg.sa.minesweeper.model.gameComponent._
import util.chaining.scalaUtilChainingOps
import play.api.libs.json.JsValue
import play.api.libs.json.Json

case class Field(matrix: Matrix[String], hidden: Matrix[String]) extends IField:
    val size = matrix.size
    val endl = sys.props("line.separator")

    def this(size: Int, filling: String)= this(new Matrix(size, filling), new Matrix(size, " "))
    def this(matrix: Matrix[String])= this(matrix, matrix)

    def bar(cellWidth: Int = 3, cellNum: Int = 3) = (("+" + "-" * cellWidth) * cellNum) + "+" + endl
    def cells(row: Int = 3, cellWidth: Int = 3) = matrix.row(row).map(_.toString).map(" " * ((cellWidth-1)/2) + _ + " " *((cellWidth -1)/2)).mkString("|","|","|") + endl
    def mesh(cellWidth: Int = 3) = (0 until size).map(cells(_, cellWidth)).mkString(bar(cellWidth, size), bar(cellWidth, size), bar(cellWidth, size))
    
    def putFlag(x: Int, y: Int) = copy(matrix.replaceCell(y, x, "F"))
    def removeFlag(x: Int, y: Int) = copy(matrix.replaceCell(y, x, "~"))

    def open(x: Int, y: Int, spiel: IGame): (IGame, IField) =
        val currentGame: Game = spiel.asInstanceOf[Game]
        val hiddenCell = this.hidden.cell(y, x)

        val gameStatus = hiddenCell match {
            case "*" =>
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

    def put(symbol: String, x: Int, y: Int) = copy(matrix.replaceCell(x, y, symbol))
    def showVisibleCell(x: Int, y: Int): String = matrix.cell(x, y)
    def showInvisibleCell(x: Int, y: Int): String =  hidden.cell(x, y)
    def isValidF(row: Int, col: Int, side: Int): Boolean = {row >= 0 && row <= side && col >= 0 && col <= side}
    def openNew(x: Int, y: Int, field: IField): IField = field.put(field.showInvisibleCell(y, x), y, x)
    
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
                val updatedField = if (currentCell ==  "~") {
                val openedField = openNew(newX, newY, currentField)
                if (invisibleCell == "0") recursiveOpen(newX, newY, openedField)
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

    def fieldToJson: String = {
        import play.api.libs.json._
        Json.prettyPrint(
            Json.obj(
                "field" -> Json.obj(
                    "size" -> this.matrix.size,
                    "matrix" -> Json.toJson(
                        for {
                            row <- 0 until this.matrix.size
                            col <- 0 until this.matrix.size
                        } yield {
                            Json.obj(
                                "row" -> row,
                                "col" -> col,
                                "cell" -> this.showVisibleCell(row, col).toString
                            )
                        }
                    ),
                    "hidden" -> Json.toJson(
                        for {
                            row <- 0 until this.matrix.size
                            col <- 0 until this.matrix.size
                        } yield {
                            Json.obj(
                                "row" -> row,
                                "col" -> col,
                                "cell" -> this.showInvisibleCell(row, col).toString
                            )
                        }
                    )
                )
            )
        )
    }

    def fieldToJson(fieldInput: IField): String = {
        import play.api.libs.json._
        Json.prettyPrint(
            Json.obj(
                "field" -> Json.obj(
                    "size" -> fieldInput.matrix.size,
                    "matrix" -> Json.toJson(
                        for {
                            row <- 0 until fieldInput.matrix.size
                            col <- 0 until fieldInput.matrix.size
                        } yield {
                            Json.obj(
                                "row" -> row,
                                "col" -> col,
                                "cell" -> fieldInput.showVisibleCell(row, col).toString
                            )
                        }
                    ),
                    "hidden" -> Json.toJson(
                        for {
                            row <- 0 until fieldInput.matrix.size
                            col <- 0 until fieldInput.matrix.size
                        } yield {
                            Json.obj(
                                "row" -> row,
                                "col" -> col,
                                "cell" -> fieldInput.showInvisibleCell(row, col).toString
                            )
                        }
                    )
                )
            )
        )
    }

    def jsonToField(jsonString: String): IField = {

        val json: JsValue = Json.parse(jsonString)
        val size = (json \ "field" \ "size").get.toString.toInt

        val fieldOption: Option[IField] = Some(new Field(size, "E"))
        val matrixOption: Option[Matrix[String]] = Some(new Matrix(size, "E"))
        val hiddenOption: Option[Matrix[String]] = Some(new Matrix(size, "E"))


        val matrix1 = matrixOption match{
            case Some(matrix) => matrix
            case None => println("Matrix is not valid"); new Matrix(size, "E")
        }

        val updatedMatrix: Matrix[String] = (0 until size * size).foldLeft(matrix1) {
            case (currentMatrix, index) =>
                val row = (json \ "field" \ "matrix" \\ "row")(index).as[Int]
                val col = (json \ "field" \ "matrix" \\ "col")(index).as[Int]
                val cell = (json \ "field" \ "matrix" \\ "cell")(index).as[String]
                currentMatrix.replaceCell(row, col, cell)
        }

        val hidden1 = hiddenOption match{
            case Some(m) => m
            case None => println("Hidden is not valid"); new Matrix(size, "E")
        }

        val updatedHidden: Matrix[String] = (0 until size * size).foldLeft(hidden1) {
            case (currentHidden, index) =>
                val row = (json \ "field" \ "hidden" \\ "row")(index).as[Int]
                val col = (json \ "field" \ "hidden" \\ "col")(index).as[Int]
                val cell = (json \ "field" \ "hidden" \\ "cell")(index).as[String]
                currentHidden.replaceCell(row, col, cell)
        }

        val finalFieldOption = fieldOption match{
            case Some(f) => Some(new Field(updatedMatrix, updatedHidden))
            case None => println("Field is not valid"); None
        }

        finalFieldOption.get
    }

    override def toString(): String = mesh()
