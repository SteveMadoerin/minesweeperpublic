package de.htwg.se.minesweeper.model.fileIoComponent.fileIoXmlImpl

import de.htwg.se.minesweeper.model.fileIoComponent.IFileIO
import de.htwg.se.minesweeper.model.gameComponent.{IGame, IField}
import de.htwg.se.minesweeper.model.gameComponent.gameBaseImpl._
import de.htwg.se.minesweeper.Default.{given}
import de.htwg.se.minesweeper.Default
import scala.xml._
import scala.compiletime.ops.string
import java.io._

class FileIO extends IFileIO {


    override def loadGame: Option[IGame] = {

        var gameOption: Option[IGame] = None
        val file = scala.xml.XML.loadFile("C:\\github\\scalacticPlayground\\minesweeper\\src\\main\\data\\game.xml")
        val status =(file \\ "game" \@ "status")
        val bombs = (file \\ "bombs").text.toInt
        val side = (file \\ "side").text.toInt
        val time = (file \\ "time").text.toInt

        val game = Default.prepareGame(bombs, side, time)

        gameOption = Some(game)
        
        gameOption match {
            case Some(game) => gameOption = Some(game)
            case None =>
        }
        gameOption
    }

    def gameToXml(game: IGame) = {
        <game status ={ game._board }>
            <bombs>{ game._bombs}</bombs>
            <side>{ game._side }</side>
            <time>{ game._time }</time>
        </game>
    }

    override def saveGame(game: IGame): Unit = {scala.xml.XML.save("C:\\github\\scalacticPlayground\\minesweeper\\src\\main\\data\\game.xml", gameToXml(game))}
    

    def stringToSymbols(s: String) = {
        s match {
            case "~" => Symbols.Covered
            case "F" => Symbols.F
            case "*" => Symbols.Bomb
            case " " => Symbols.Empty
            case "0" => Symbols.Zero
            case "1" => Symbols.One
            case "2" => Symbols.Two
            case "3" => Symbols.Three
            case "4" => Symbols.Four
            case "5" => Symbols.Five
            case "6" => Symbols.Six
            case "7" => Symbols.Seven
            case "8" => Symbols.Eight
            case _ => Symbols.E
        }
    }

    override def loadField2: Option[IField] = {
        var fieldOption: Option[IField] = None
        var matrixOption: Option[Matrix[Symbols]] = None
        var hiddenOption: Option[Matrix[Symbols]] = None

        val file = scala.xml.XML.loadFile("C:\\github\\scalacticPlayground\\minesweeper\\src\\main\\data\\field.xml")
        val size = (file \\ "field" \@ "size").toInt

        size match{
            case _ => 
                matrixOption = Some(Default.scalableMatrix(size, Symbols.Covered))
                hiddenOption = Some(Default.scalableMatrix(size, Symbols.Covered))
                fieldOption = Some(Default.scalableField(size, Symbols.Covered)) 
        }

        val cellNodesVisible: NodeSeq = (file \\ "field" \\ "matrix" \\ "cell")
        var matrix = matrixOption match{
            case Some(m) => m
            case None => println("Matrix is not valid"); Default.scalableMatrix(size, Symbols.E)
        }
   
        var _matrix = matrix
        for(cell <- cellNodesVisible){
            val row: Int = (cell \ "@row").text.toInt
            val col: Int = (cell \ "@col").text.toInt
            val symbols: String = cell.text.trim
            _matrix = _matrix.replaceCell(row, col, stringToSymbols(symbols))
        }

        val cellNodesHidden :NodeSeq = (file \\ "field" \\ "hidden" \\ "cell")
        var hidden = matrixOption match{
            case Some(m) => m
            case None => println("hidden is not Valid"); Default.scalableMatrix(size, Symbols.E)
        }

        var _hidden = hidden
        for(cell <- cellNodesHidden){
            val row: Int = (cell \"@row").text.toInt
            val col: Int = (cell \ "@col").text.toInt
            val symbols: String = cell.text.trim
            _hidden = _hidden.replaceCell(row, col, stringToSymbols(symbols))
        }
        
        fieldOption match{
            case Some(f) =>{
                var field = Default.mergeMatrixToField(_matrix, _hidden)
                fieldOption = Some(field)
            }
            case None => println("field is not Valid");
        }

        fieldOption

    }

    def fieldToXml(field: IField) = {

        <field size = {field.matrix.size.toString}>
            <matrix>
            {
                for {
                    row <- 0 until field.matrix.size
                    col <- 0 until field.matrix.size
                } yield cellToXmlVisible(field, row, col)
            }
            </matrix>
            <hidden>
            {
                for {
                    row <- 0 until field.matrix.size
                    col <- 0 until field.matrix.size
                } yield cellToXmlHidden(field, row, col)
            }
            </hidden>
        </field>
    }

    def cellToXmlVisible(field: IField, row: Int, col: Int) = {<cell row={ row.toString } col={ col.toString }>{ field.showVisibleCell(row, col).toString }</cell>}
    def cellToXmlHidden(field: IField, row: Int, col: Int) = {<cell row={ row.toString } col={ col.toString }>{ field.showInvisibleCell(row, col).toString }</cell>}
    
    def saveField(field: IField): Unit = saveString(field)

    def saveString(field: IField): Unit = {
        import java.io._

        val pw = new PrintWriter(new File("C:\\github\\scalacticPlayground\\minesweeper\\src\\main\\data\\field.xml"))
        val prettyPrinter = new PrettyPrinter(120, 4)
        val hiddenField = new Field(field._hidden)
        val visibleField = new Field(field.matrix)
        val xml = prettyPrinter.format(fieldToXml(field))
        pw.write(xml)
        pw.close
    }

    def loadPlayerScores(filePath: String): Seq[(String, Int)] = {
        val file = new File(filePath)

        if (file.exists() && file.length() != 0) {
            val rootElem: Elem = XML.loadFile(file)

            val scores = (rootElem \ "score").map { scoreElem =>
            val playerName = (scoreElem \ "player").text
            val score = (scoreElem \ "value").text.toInt
            (playerName, score)
            }

            scores.sortBy { case (_, score) => -score }.take(10)
        } else {
            Seq.empty
        }
    }

    def savePlayerScore(playerName: String, score: Int, filePath: String): Unit = {
        
        val file = new File(filePath)

        val rootElem: Elem = if (file.exists() && file.length() != 0) {
            XML.loadFile(file)
        } else {
            <scores></scores>
        }

        val newScoreElem = <score>
            <player>{playerName}</player>
            <value>{score}</value>
        </score>

        val updatedRootElem = rootElem.copy(child = rootElem.child ++ Seq(newScoreElem))
        val printer = new PrettyPrinter(80, 2)
        val writer = new PrintWriter(new FileWriter(file))
        try {
            writer.write(printer.format(updatedRootElem))
        } finally {
            writer.close() // Ensure the writer is closed to flush the output
        }
    }
    
}