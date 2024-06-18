package de.htwg.sa.minesweeper.persistence.persistenceComponent.persistenceXmlImpl

import de.htwg.sa.minesweeper.persistence.persistenceComponent.IPersistence
import de.htwg.sa.minesweeper.persistence.entity.*
import de.htwg.sa.minesweeper.persistence.persistenceComponent.config.Default

import java.io._
import scala.util.{Failure, Success, Try}
import scala.xml._

class Persistence extends IPersistence {

    override def loadGame: Option[IGame] = {
        val maybeFile: Try[Elem] = Try(scala.xml.XML.loadFile("C:\\Playground\\minesweeperpublic\\src\\main\\data\\game.xml"))
        val file = maybeFile match {
            case Success(file) => file
            case Failure(exception) => throw exception
        }

        val status = (file \\ "game" \@ "status")
        val bombs = (file \\ "bombs").text.toInt
        val side = (file \\ "side").text.toInt
        val time = (file \\ "time").text.toInt

        Some(Game(bombs, side, time, "Playing"))
    }

    def gameToXml(game: IGame) = {
        <game status ={ game.board }>
            <bombs>{ game.bombs}</bombs>
            <side>{ game.side }</side>
            <time>{ game.time }</time>
        </game>
    }

    override def saveGame(game: IGame): Unit = {scala.xml.XML.save("C:\\Playground\\minesweeperpublic\\src\\main\\data\\game.xml", gameToXml(game))}
    
    def loadField(field: String): Option[IField] = {
        val maybeFile: Try[Elem] = Try(scala.xml.XML.loadFile("C:\\Playground\\minesweeperpublic\\src\\main\\data\\field.xml"))
        val file = maybeFile match {
            case Success(file) => file
            case Failure(exception) => throw exception
        }

        val size = (file \\ "field" \@ "size").toInt
        val matrixOption = Some(Default.scalableMatrix(size, "~"))
        val hiddenOption = Some(Default.scalableMatrix(size, "~"))
        val fieldOption = Some(Default.scalableField(size, "~"))

        val cellNodesVisible: NodeSeq = (file \\ "field" \\ "matrix" \\ "cell")
        val matrix = cellNodesVisible.foldLeft(matrixOption.get) { (m, cell) =>
            val row: Int = (cell \ "@row").text.toInt
            val col: Int = (cell \ "@col").text.toInt
            val symbol = (cell.text.trim)
            m.replaceCell(row, col, symbol)
        }

        val cellNodesHidden: NodeSeq = (file \\ "field" \\ "hidden" \\ "cell")
        val hidden = cellNodesHidden.foldLeft(hiddenOption.get) { (h, cell) =>
            val row: Int = (cell \ "@row").text.toInt
            val col: Int = (cell \ "@col").text.toInt
            val symbol = (cell.text.trim)
            h.replaceCell(row, col, symbol)
        }

        fieldOption.map { f => Default.mergeMatrixToField(matrix, hidden) }
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

        val pw = Try(new PrintWriter(new File("C:\\Playground\\minesweeperpublic\\src\\main\\data\\field.xml")))
        pw match {
            case Success(pw) =>
                val prettyPrinter = new PrettyPrinter(120, 4)
                val hiddenField = new Field(field.hidden)
                val visibleField = new Field(field.matrix)
                val xml = prettyPrinter.format(fieldToXml(field))
                pw.write(xml)
            case Failure(exception) => throw exception
        }
        pw.get.close
    }

    def loadPlayerScores(filePath: String): Seq[(String, Int)] = {
        
        val tryLoadScores = Try {
            val file = new File(filePath)
            val rootElem: Elem = XML.loadFile(file)
            val scores = (rootElem \ "score").map { scoreElem =>
            val playerName = (scoreElem \ "player").text
            val score = (scoreElem \ "value").text.toInt
            (playerName, score)
            }
            scores.sortBy { case (_, score) => -score }.take(10)
        }

        tryLoadScores match {
            case Success(scores) => scores
            case Failure(exception) =>
                println(s"An error occurred while loading scores: ${exception.getMessage}")
                Seq.empty
        }
    }

    def savePlayerScore(playerName: String, score: Int, filePath: String): Unit = {
        
        val maybeFile = Try(new File(filePath))
        val file = maybeFile.getOrElse(throw new Exception("File not found"))

        val maybeRootElement = Try {
            val rootElem: Elem = XML.loadFile(file)
            rootElem
        }

        val rootElem = maybeRootElement.getOrElse( <scores></scores>)

        val newScoreElem = <score>
            <player>{playerName}</player>
            <value>{score}</value>
        </score>

        val updatedRootElem = rootElem.copy(child = rootElem.child ++ Seq(newScoreElem))

        Try {
            val printer = new PrettyPrinter(80, 2)
            val writer = new PrintWriter(new FileWriter(file))
            writer.write(printer.format(updatedRootElem))
            writer.close()
        }
    }

    override def loadField: Option[IField] = {
        val maybeFile: Try[Elem] = Try(scala.xml.XML.loadFile("C:\\Playground\\minesweeperpublic\\src\\main\\data\\field.xml"))
        val file = maybeFile match {
            case Success(file) => file
            case Failure(exception) => throw exception
        }

        val size = (file \\ "field" \@ "size").toInt
        val matrixOption = Some(Default.scalableMatrix(size, "~"))
        val hiddenOption = Some(Default.scalableMatrix(size, "~"))
        val fieldOption = Some(Default.scalableField(size, "~"))

        val cellNodesVisible: NodeSeq = (file \\ "field" \\ "matrix" \\ "cell")
        val matrix = cellNodesVisible.foldLeft(matrixOption.get) { (m, cell) =>
            val row: Int = (cell \ "@row").text.toInt
            val col: Int = (cell \ "@col").text.toInt
            val symbol = (cell.text.trim)
            m.replaceCell(row, col, symbol)
        }

        val cellNodesHidden: NodeSeq = (file \\ "field" \\ "hidden" \\ "cell")
        val hidden = cellNodesHidden.foldLeft(hiddenOption.get) { (h, cell) =>
            val row: Int = (cell \ "@row").text.toInt
            val col: Int = (cell \ "@col").text.toInt
            val symbol = (cell.text.trim)
            h.replaceCell(row, col, symbol)
        }

        fieldOption.map { f => Default.mergeMatrixToField(matrix, hidden) }
    }
}