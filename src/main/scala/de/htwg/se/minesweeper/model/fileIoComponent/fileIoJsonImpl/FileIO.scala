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
                    "status" -> game.board,
                    "bombs" -> game.bombs,
                    "side" -> game.side,
                    "time" -> game.time
                )
            )
        )
        
    }

    //def initGame(using game: IGame) = game

    override def loadGame: Option[IGame] = 
        import java.io._
        val source: String = Source.fromFile("C:\\github\\scalacticPlayground\\minesweeper\\src\\main\\data\\game.json").getLines.mkString
        val json: JsValue = Json.parse(source)
        val status = (json \ "game" \ "status").get.toString
        val bombs = (json \ "game" \ "bombs").get.toString.toInt
        val side = (json \ "game" \ "side").get.toString.toInt
        val time = (json \ "game" \ "time").get.toString.toInt
        val game = Default.prepareGame(bombs, side, time)

        val gameOption: Option[IGame] = Some(game)

        val newGameOption = gameOption match {
            case Some(game) => Some(game)
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
                    "size" -> field.matrix.size,
                    "matrix" -> Json.toJson(
                        for {
                            row <- 0 until field.matrix.size
                            col <- 0 until field.matrix.size
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
                            row <- 0 until field.matrix.size
                            col <- 0 until field.matrix.size
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

        val source: String = Source.fromFile("C:\\github\\scalacticPlayground\\minesweeper\\src\\main\\data\\field.json").getLines.mkString
        val json: JsValue = Json.parse(source)
        val size = (json \ "field" \ "size").get.toString.toInt

        val fieldOption: Option[IField] = Some(Default.scalableField(size, Symbols.E))
        val matrixOption: Option[Matrix[Symbols]] = Some(Default.scalableMatrix(size, Symbols.E))
        val hiddenOption: Option[Matrix[Symbols]] = Some(Default.scalableMatrix(size, Symbols.E))


        val matrix1 = matrixOption match{
            case Some(matrix) => matrix
            case None => println("Matrix is not valid"); Default.scalableMatrix(size, Symbols.E)
        }

        val updatedMatrix: Matrix[Symbols] = (0 until size * size).foldLeft(matrix1) {
            case (currentMatrix, index) =>
                val row = (json \ "field" \ "matrix" \\ "row")(index).as[Int]
                val col = (json \ "field" \ "matrix" \\ "col")(index).as[Int]
                val cell = (json \ "field" \ "matrix" \\ "cell")(index).as[String]
                currentMatrix.replaceCell(row, col, stringToSymbols(cell))
        }

        val hidden1 = hiddenOption match{
            case Some(m) => m
            case None => println("Hidden is not valid"); Default.scalableMatrix(size, Symbols.E)
        }

        val updatedHidden: Matrix[Symbols] = (0 until size * size).foldLeft(hidden1) {
            case (currentHidden, index) =>
                val row = (json \ "field" \ "hidden" \\ "row")(index).as[Int]
                val col = (json \ "field" \ "hidden" \\ "col")(index).as[Int]
                val cell = (json \ "field" \ "hidden" \\ "cell")(index).as[String]
                currentHidden.replaceCell(row, col, stringToSymbols(cell))
        }

        val finalFieldOption = fieldOption match{
            case Some(f) => Some(Default.mergeMatrixToField(updatedMatrix, updatedHidden))
            case None => println("Field is not valid"); None
        }

        finalFieldOption
    }

    def loadPlayerScores(filePath: String): Seq[(String, Int)] = {
        val file = new File(filePath)
        //var scores: Seq[(String, Int)] = Seq.empty

        val finalScores: Seq[(String, Int)] = if (file.exists() && file.length() != 0) {
            val source = Source.fromFile(file)
            val scoresJson = try {
                Json.parse(source.mkString)
            } finally {
                source.close()
            }

            val scores = scoresJson.as[JsArray].value.flatMap { scoreJson =>
            for {
                playerName <- (scoreJson \ "player").validate[String].asOpt
                score <- (scoreJson \ "score").validate[Int].asOpt
            } yield (playerName, score)
            }.toSeq
            scores
        } else {
            val scores = Seq.empty
            scores
        }
        finalScores
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