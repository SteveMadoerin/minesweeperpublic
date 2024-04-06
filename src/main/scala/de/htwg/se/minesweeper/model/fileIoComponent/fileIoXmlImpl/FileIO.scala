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

    override def loadGame: GameBox = {
        // TRY OPTION for file
        val file = scala.xml.XML.loadFile("C:\\github\\scalacticPlayground\\minesweeper\\src\\main\\data\\game.xml")
        val status = (file \\ "game" \@ "status")
        val bombs = (file \\ "bombs").text.toInt
        val side = (file \\ "side").text.toInt
        val time = (file \\ "time").text.toInt
        // HERE IS THE TWO TRACK CODE
        val gameBox = GameBox(Some(new Game(0, 0, 0, ""))).insertBomb(bombs).insertSide(side).insertTime(time)
        gameBox
        //val game = Default.prepareGame(bombs, side, time)
        //val gameOption: Option[IGame] = Some(game)
        //gameOption
    }

/*     override def loadGame: Option[IGame] = {
        
        val file = scala.xml.XML.loadFile("C:\\github\\scalacticPlayground\\minesweeper\\src\\main\\data\\game.xml")
        val status = (file \\ "game" \@ "status")
        val bombs = (file \\ "bombs").text.toInt
        val side = (file \\ "side").text.toInt
        val time = (file \\ "time").text.toInt

        val game = Default.prepareGame(bombs, side, time)
        val gameOption: Option[IGame] = Some(game)
        gam */

    def gameToXml(game: IGame) = {
        <game status ={ game.board }>
            <bombs>{ game.bombs}</bombs>
            <side>{ game.side }</side>
            <time>{ game.time }</time>
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
    
    override def loadField: Option[IField] = {
        val file = scala.xml.XML.loadFile("C:\\github\\scalacticPlayground\\minesweeper\\src\\main\\data\\field.xml")
        val size = (file \\ "field" \@ "size").toInt

        val matrixOption = Some(Default.scalableMatrix(size, Symbols.Covered))
        val hiddenOption = Some(Default.scalableMatrix(size, Symbols.Covered))
        val fieldOption = Some(Default.scalableField(size, Symbols.Covered))

        val cellNodesVisible: NodeSeq = (file \\ "field" \\ "matrix" \\ "cell")
        val matrix = cellNodesVisible.foldLeft(matrixOption.get) { (m, cell) =>
            val row: Int = (cell \ "@row").text.toInt
            val col: Int = (cell \ "@col").text.toInt
            val symbol = stringToSymbols(cell.text.trim)
            m.replaceCell(row, col, symbol)
        }

        val cellNodesHidden: NodeSeq = (file \\ "field" \\ "hidden" \\ "cell")
        val hidden = cellNodesHidden.foldLeft(hiddenOption.get) { (h, cell) =>
            val row: Int = (cell \ "@row").text.toInt
            val col: Int = (cell \ "@col").text.toInt
            val symbol = stringToSymbols(cell.text.trim)
            h.replaceCell(row, col, symbol)
        }

        fieldOption.map { f =>
            Default.mergeMatrixToField(matrix, hidden)
        }
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
        val hiddenField = new Field(field.hidden)
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
            writer.close()
        }
    }
    
}