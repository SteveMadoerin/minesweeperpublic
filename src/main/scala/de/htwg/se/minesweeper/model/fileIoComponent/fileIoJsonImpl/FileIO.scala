package de.htwg.se.minesweeper.model.fileIoComponent.fileIoJsonImpl

import de.htwg.se.minesweeper.model.fileIoComponent.IFileIO
import de.htwg.se.minesweeper.model.gameComponent.{IGame, IField}
import scala.io.Source
import java.io._
import play.api.libs.json._
import de.htwg.se.minesweeper.Default.{given}
import de.htwg.se.minesweeper.Default
import de.htwg.se.minesweeper.model.gameComponent.gameBaseImpl.Symbols
import de.htwg.se.minesweeper.model.gameComponent.gameBaseImpl.Matrix


class FileIO extends IFileIO{
    
    override def saveGame(game: IGame): Unit = {
        val pw = new PrintWriter(new File("C:\\github\\scalacticPlayground\\minesweeper\\src\\main\\data\\game.json"))
        pw.write(gameToJson(game).+('\n'))
        pw.close
    }


    def gameToJson(game: IGame): String = {
        Json.prettyPrint(
            Json.obj(
                "game" -> Json.obj(
                    "status" -> game._board,
                    "bombs" -> game.getBombs,
                    "side" -> game.getSide,
                    "time" -> game.getTime
                )
            )
        )
        
    }

    def initGame(using game: IGame) = game

    override def loadGame: Option[IGame] = 
        import java.io._
        var gameOption: Option[IGame] = None
        val source: String = Source.fromFile("C:\\github\\scalacticPlayground\\minesweeper\\src\\main\\data\\game.json").getLines.mkString
        val json: JsValue = Json.parse(source)
        val status = (json \ "game" \ "status").get.toString
        val bombs = (json \ "game" \ "bombs").get.toString.toInt
        val side = (json \ "game" \ "side").get.toString.toInt
        val time = (json \ "game" \ "time").get.toString.toInt
        var game = initGame

        game.setTime(time)
        game.setSideAndBombs(side, bombs)

        gameOption = Some(game)

        gameOption match {
            case Some(game) => gameOption = Some(game)
            case None =>
        }
        gameOption
    

    override def saveField(field: IField): Unit = 
        import java.io._
        val pw = new PrintWriter(new File("C:\\github\\scalacticPlayground\\minesweeper\\src\\main\\data\\field.json"))
        pw.write(fieldToJson(field))
        pw.close
    
    def fieldToJson(field: IField): String = {
        import play.api.libs.json._
        Json.prettyPrint(
            Json.obj(
                "field" -> Json.obj(
                    "size" -> field._matrix.size,
                    "matrix" -> Json.toJson(
                        for {
                            row <- 0 until field._matrix.size
                            col <- 0 until field._matrix.size
                        } yield {
                            Json.obj(
                                "row" -> row,
                                "col" -> col,
                                "cell" -> field.showVisibleCell(row, col).toString
                            )
                        }
                    ),
                    "hidden" -> Json.toJson(
                        for {
                            row <- 0 until field._matrix.size
                            col <- 0 until field._matrix.size
                        } yield {
                            Json.obj(
                                "row" -> row,
                                "col" -> col,
                                "cell" -> field.showInvisibleCell(row, col).toString
                            )
                        }
                    )
                )
            )
        )
    }

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

        val source: String = Source.fromFile("C:\\github\\scalacticPlayground\\minesweeper\\src\\main\\data\\field.json").getLines.mkString
        val json: JsValue = Json.parse(source)
        val size = (json \ "field" \ "size").get.toString.toInt

        size match{
            case _ => 
                matrixOption = Some(Default.scalableMatrix(size, Symbols.E))
                hiddenOption = Some(Default.scalableMatrix(size, Symbols.E))
                fieldOption = Some(Default.scalableField(size, Symbols.E))
        }

        var matrix = matrixOption match{
            case Some(matrix) => matrix
            case None => println("Matrix is not valid"); Default.scalableMatrix(size, Symbols.E)
        }

        var _matrix = matrix
        for(index <- 0 until size * size){
            val row = (json \ "field" \ "matrix" \\ "row")(index).as[Int]
            val col = (json \ "field" \ "matrix" \\ "col")(index).as[Int]
            val cell = (json \ "field" \ "matrix" \\ "cell")(index).as[String]
            _matrix = _matrix.replaceCell(row, col, stringToSymbols(cell))
        }

        var hidden = hiddenOption match{
            case Some(m) => m
            case None => println("Hidden is not valid"); Default.scalableMatrix(size, Symbols.E)
        }

        var _hidden = hidden
        for(index <- 0 until (size * size)){
            val row = (json \ "field" \ "hidden" \\ "row")(index).as[Int]
            val col = (json \ "field" \ "hidden" \\ "col")(index).as[Int]
            val cell = (json \ "field" \ "hidden" \\ "cell")(index).as[String]
            _hidden = _hidden.replaceCell(row, col, stringToSymbols(cell))
        }

        fieldOption match{
            case Some(f) => fieldOption = Some(Default.mergeMatrixToField(_matrix, _hidden))
            case None => println("Field is not valid")
        }

        fieldOption
    }

    def loadPlayerScores(filePath: String): Seq[(String, Int)] = {
        val file = new File(filePath)
        var scores: Seq[(String, Int)] = Seq.empty

        if (file.exists() && file.length() != 0) {
            val source = Source.fromFile(file)
            val scoresJson = try {
                Json.parse(source.mkString)
            } finally {
                source.close()
            }

            scores = scoresJson.as[JsArray].value.flatMap { scoreJson =>
            for {
                playerName <- (scoreJson \ "player").validate[String].asOpt
                score <- (scoreJson \ "score").validate[Int].asOpt
            } yield (playerName, score)
            }.toSeq
        } else {
            scores = Seq.empty
        }
        scores
    }

    def savePlayerScore(playerName: String, score: Int, filePath: String): Unit = {
        val file = new File(filePath)

        val scoresArray: JsArray = if (file.exists() && file.length() != 0) {
            val source = Source.fromFile(file)
            try {
                Json.parse(source.mkString).as[JsArray]
            } finally {
                source.close()
            }
        } else {
            JsArray()
        }

        val newScoreObj = Json.obj(
            "player" -> playerName,
            "score" -> score
        )

        val updatedScoresArray = scoresArray :+ newScoreObj

        val writer = new PrintWriter(new FileWriter(file))
        try {
            writer.write(Json.prettyPrint(updatedScoresArray))
        } finally {
            writer.close()
        }
    } 

}