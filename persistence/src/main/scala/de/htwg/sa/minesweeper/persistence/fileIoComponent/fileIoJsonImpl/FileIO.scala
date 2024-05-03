package de.htwg.sa.minesweeper.persistence.fileIoComponent.fileIoJsonImpl

import de.htwg.sa.minesweeper.persistence.fileIoComponent.IFileIO
import de.htwg.sa.minesweeper.model.gameComponent.{IGame, IField}
import scala.io.Source
import java.io._
import play.api.libs.json._
/* import de.htwg.sa.minesweeper.Default.{given}
import de.htwg.sa.minesweeper.Default */
import de.htwg.sa.minesweeper.model.gameComponent.gameBaseImpl._
import scala.util.{Try, Success, Failure}
import de.htwg.sa.minesweeper.persistence.fileIoComponent.config.Default


class FileIO extends IFileIO{

    
    override def saveGame(game: IGame): Unit = {
        val pw = Try (new PrintWriter(new File("C:\\github\\scalacticPlayground\\minesweeper\\src\\main\\data\\game.json")))
        pw match {
            case Success(pw) => pw.write(gameToJson(game).+('\n'))
            case Failure(e) => println("Error: " + e)
        }
        pw.get.close
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

    override def loadGame: Option[IGame] = 
        import java.io._
        val maybeSource = Try(Source.fromFile("C:\\github\\scalacticPlayground\\minesweeper\\src\\main\\data\\game.json").getLines.mkString)
        val source = maybeSource match {
            case Success(source) => source
            case Failure(exception) => throw exception
        }

        val maybeJson: Try[JsValue] = Try(Json.parse(source))
        val json = maybeJson match {
            case Success(json) => json
            case Failure(exception) => throw exception
        }

        val status = (json \ "game" \ "status").get.toString
        val bombs = (json \ "game" \ "bombs").get.toString.toInt
        val side = (json \ "game" \ "side").get.toString.toInt
        val time = (json \ "game" \ "time").get.toString.toInt
        // TWO TRACK CODE   
        Some(new Game(bombs, side, time, "Playing")) // after loading should always be playing
    
    override def saveField(field: IField) = {
        import java.io._
        val pw = Try (new PrintWriter(new File("C:\\github\\scalacticPlayground\\minesweeper\\src\\main\\data\\field.json")))
        pw match {
            case Success(pw) => pw.write(fieldToJson(field))
            case Failure(e) => println("Error: " + e)
        }
        pw.get.close
    } 

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

    override def loadField: Option[IField] = {

        val source: String = Source.fromFile("C:\\github\\scalacticPlayground\\minesweeper\\src\\main\\data\\field.json").getLines.mkString
        val json: JsValue = Json.parse(source)
        val size = (json \ "field" \ "size").get.toString.toInt

        val fieldOption: Option[IField] = Some(Default.scalableField(size, "E"))
        val matrixOption: Option[Matrix[String]] = Some(Default.scalableMatrix(size, "E"))
        val hiddenOption: Option[Matrix[String]] = Some(Default.scalableMatrix(size, "E"))


        val matrix1 = matrixOption match{
            case Some(matrix) => matrix
            case None => println("Matrix is not valid"); Default.scalableMatrix(size, "E")
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
            case None => println("Hidden is not valid"); Default.scalableMatrix(size, "E")
        }

        val updatedHidden: Matrix[String] = (0 until size * size).foldLeft(hidden1) {
            case (currentHidden, index) =>
                val row = (json \ "field" \ "hidden" \\ "row")(index).as[Int]
                val col = (json \ "field" \ "hidden" \\ "col")(index).as[Int]
                val cell = (json \ "field" \ "hidden" \\ "cell")(index).as[String]
                currentHidden.replaceCell(row, col, cell)
        }

        val finalFieldOption = fieldOption match{
            case Some(f) => Some(Default.mergeMatrixToField(updatedMatrix, updatedHidden))
            case None => println("Field is not valid"); None
        }

        finalFieldOption
    }

    def loadPlayerScores(filePath: String): Seq[(String, Int)] = {

        val maybeFinalScores: Try[Seq[(String, Int)]] = Try {
            val file = new File(filePath)
            val source = Source.fromFile(file)
            val scoresJson = Try(Json.parse(source.mkString)).getOrElse(JsArray())
            source.close()

            scoresJson.as[JsArray].value.flatMap { scoreJson =>
            for {
                playerName <- (scoreJson \ "player").validate[String].asOpt
                score <- (scoreJson \ "score").validate[Int].asOpt
            } yield (playerName, score)
            }.toSeq
            
        }
        maybeFinalScores.getOrElse(Seq.empty)
    }

    // try to make only a JsArray
    def savePlayerScore(playerName: String, score: Int, filePath: String): Unit = {
        
        val maybeFile =  Try{ new File(filePath) } 
        val file = maybeFile match {
            case Success(f) => f
            case Failure(exception) => throw exception
        }
        val maybeScoresArray: Try[JsArray] = Try {
            val source = Source.fromFile(file)
            val scoresArrayNew = Try(Json.parse(source.mkString)).getOrElse(JsArray()).as[JsArray]
            source.close()
            scoresArrayNew
        }

        val scoresArray = maybeScoresArray.getOrElse(JsArray())

        val newScoreObj = Json.obj(
            "player" -> playerName,
            "score" -> score
        )

        val updatedScoresArray = scoresArray :+ newScoreObj

        val writer = Try(new PrintWriter(new FileWriter(file)))
        writer.foreach { w =>
            Try {
                w.write(Json.prettyPrint(updatedScoresArray))
            }.foreach { _ =>
                w.close()
            }

        }
    } 
}