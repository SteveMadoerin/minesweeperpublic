package de.htwg.sa.minesweeper.ui.gui

import de.htwg.sa.minesweeper.ui.*
import play.api.libs.json.{Format, JsError, JsObject, JsResult, JsString, JsSuccess, JsValue, Json}

import scala.io.Source
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.HttpMethods
import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.model.ContentTypes
import akka.http.scaladsl.Http

import scala.concurrent.Future
import akka.http.scaladsl.model.HttpResponse

import scala.concurrent.duration.*
import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContextExecutor
import akka.actor.ActorSystem
import akka.stream.Materializer

import scala.concurrent.Await
import scala.annotation.internal.Body
import akka.util.ByteString
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.*
import akka.stream.ActorMaterializer

import scala.concurrent.Future
import scala.util.{Failure, Success}
import scala.util.{Failure, Success, Try}
import akka.http.scaladsl.unmarshalling.Unmarshal
import de.htwg.sa.minesweeper.ui.model.{FieldTui, GameTui, MatrixTui, Move}

import scala.concurrent.Future
import scala.concurrent.duration.*

object RestUtil {

    implicit val system: ActorSystem = ActorSystem()
    implicit val materializer: Materializer = Materializer(system)
    implicit val executionContext: ExecutionContextExecutor = system.dispatcher

    def requestNewGame(side: Int, bombs: Int): (FieldTui, GameTui) = {

        val url = "http://controller:9081/controller/newGameForGui" + s"?side=$side&bombs=$bombs"
        val request = HttpRequest(
            method = HttpMethods.PUT,
            uri = url
        )

        val responseFuture: Future[HttpResponse] = Http().singleRequest(request)
        val bodyStringFuture: Future[String] = responseFuture.flatMap { response =>
            response.entity.toStrict(5.seconds).map(_.data.utf8String)
        }
        val bodyString = Await.result(bodyStringFuture, 5.seconds)
        val (newGame, newField): (GameTui, FieldTui) = jsonToGameAndField(bodyString)
        (newField, newGame)
    }

    def jsonToGameAndField(jsonString: String): (GameTui, FieldTui) = {
        val json: JsValue = Json.parse(jsonString)
        val jsonGame: Option[JsValue] = (json \\ "game").headOption
        val status = (jsonGame.get \ "status").get.toString
        val bombs = (jsonGame.get \ "bombs").get.toString.toInt
        val side = (jsonGame.get \ "side").get.toString.toInt
        val time = (jsonGame.get \ "time").get.toString.toInt
        val statusWithoutQuotes = status.replace("\"", "")
        val gameTui = GameTui(bombs, side, time, statusWithoutQuotes)

        val jsonField: Option[JsValue] = (json \\ "field").headOption

        val jsonValue = jsonField.get

        val size = (jsonValue \ "size").get.toString.toInt

        val fieldVectorOption: Option[FieldTui] = Some(FieldTui(MatrixTui(Vector.tabulate(size, size) { (row, col) => "E" }), (MatrixTui(Vector.tabulate(size, size) { (row, col) => "E" }))))
        val matrixVectorOption: Option[Vector[Vector[String]]] = Some(Vector.tabulate(size, size) { (row, col) => "E" })
        val hiddenVectorOption: Option[Vector[Vector[String]]] = Some(Vector.tabulate(size, size) { (row, col) => "E" })

        val matrixVector1 = matrixVectorOption match {
            case Some(matrix) => matrix
            case None => println("Matrix is not valid"); Vector.tabulate(size, size) { (row, col) => "E" }
        }

        val updatedMatrixVector: Vector[Vector[String]] = (0 until size * size).foldLeft(matrixVector1) {
            case (currentMatrix, index) =>
                val row = (jsonValue \ "matrix" \\ "row")(index).as[Int]
                val col = (jsonValue \ "matrix" \\ "col")(index).as[Int]
                val cell = (jsonValue \ "matrix" \\ "cell")(index).as[String]
                currentMatrix.updated(row, currentMatrix(row).updated(col, cell))
        }

        val hiddenVector1 = hiddenVectorOption match {
            case Some(m) => m
            case None => println("Hidden is not valid"); Vector.tabulate(size, size) { (row, col) => "E" }
        }

        val updatedHiddenVector: Vector[Vector[String]] = (0 until size * size).foldLeft(hiddenVector1) {
            case (currentHidden, index) =>
                val row = (jsonValue \ "hidden" \\ "row")(index).as[Int]
                val col = (jsonValue \ "hidden" \\ "col")(index).as[Int]
                val cell = (jsonValue \ "hidden" \\ "cell")(index).as[String]
                currentHidden.updated(row, currentHidden(row).updated(col, cell))
        }

        val finalFieldOption = fieldVectorOption match {
            case Some(f) => Some(FieldTui(MatrixTui(updatedMatrixVector), MatrixTui(updatedHiddenVector)))
            case None => println("Field is not valid"); None
        }

        (gameTui, finalFieldOption.get)
    }

    def requestBoardBounds: Int = {
        println("requestBoardBounds successful")
        requestControllerField.matrix.rows.size-1
    }

    def requestControllerField = {

        val url = "http://controller:9081/controller/field"
        
        val request = HttpRequest(
            method =  HttpMethods.GET,
            uri = url
        )

        val bodyString = Await.result(Http().singleRequest(request).flatMap(_.entity.toStrict(5.seconds).map(_.data.utf8String)), 5.seconds)
        val field = jsonToFieldTui(bodyString)
        field
    }

    def requestControllerGame = {

        val url = "http://controller:9081/controller/game"

        val request = HttpRequest(
            method = HttpMethods.GET,
            uri = url
        )
        val bodyString = Await.result(Http().singleRequest(request).flatMap(_.entity.toStrict(5.seconds).map(_.data.utf8String)), 5.seconds)
        jsonToGameTui(bodyString)
    }

    def requestExit = {
        val url = "http://controller:9081/controller/exit"

        val request = HttpRequest(
            method = HttpMethods.PUT,
            uri = url
        )
    }

    def requestNewGameGui = {
        val url = "http://controller:9081/controller/newGameGui"

        val request = HttpRequest(
            method = HttpMethods.GET,
            uri = url
        )
    }

    def requestControllerLoadGame: Unit = {

        val url = "http://controller:9081/controller/loadGame"

        val request = HttpRequest(
            method = HttpMethods.GET,
            uri = url
        )

        val responseFuture: Future[HttpResponse] = Http().singleRequest(request)
        val bodyStringFuture: Future[String] = responseFuture.flatMap { response =>
            response.entity.toStrict(5.seconds).map(_.data.utf8String)
        }

        val bodyStringLoad = Await.result(bodyStringFuture, 5.seconds)
        println(bodyStringLoad)
    }

    def requestControllerSaveGame: Unit = {

        val url = "http://controller:9081/controller/saveGame"

        val request = HttpRequest(
            method = HttpMethods.GET,
            uri = url
        )

        val responseFuture: Future[HttpResponse] = Http().singleRequest(request)
        val bodyStringFuture: Future[String] = responseFuture.flatMap { response =>
            response.entity.toStrict(5.seconds).map(_.data.utf8String)
        }

        val bodyStringSave = Await.result(bodyStringFuture, 5.seconds)
        println(bodyStringSave)
    }

    def requestControllerMakeAndPublishUndo: Unit = {
        val url = "http://controller:9081/controller/makeAndPublish/undo"
        val request = HttpRequest(
            method = HttpMethods.GET,
            uri = url
        )

        val responseFuture: Future[HttpResponse] = Http().singleRequest(request)
        val bodyStringFuture: Future[String] = responseFuture.flatMap { response =>
            response.entity.toStrict(5.seconds).map(_.data.utf8String)
        }

        val bodyStringUndo = Await.result(bodyStringFuture, 5.seconds)
        println(bodyStringUndo)
    }

    def requestControllerMakeAndPublishRedo: Unit = {

        val url = "http://controller:9081/controller/makeAndPublish/redo"
        val request = HttpRequest(
            method = HttpMethods.GET,
            uri = url
        )

        val responseFuture: Future[HttpResponse] = Http().singleRequest(request)
        val bodyStringFuture: Future[String] = responseFuture.flatMap { response =>
            response.entity.toStrict(5.seconds).map(_.data.utf8String)
        }

        val bodyStringUndo = Await.result(bodyStringFuture, 5.seconds)
        println(bodyStringUndo)
    }

    def requestControllerCheat: Unit = {

        val url = "http://controller:9081/controller/cheat"

        val request = HttpRequest(
            method = HttpMethods.GET,
            uri = url
        )

        val responseFuture: Future[HttpResponse] = Http().singleRequest(request)
        val bodyStringFuture: Future[String] = responseFuture.flatMap { response =>
            response.entity.toStrict(5.seconds).map(_.data.utf8String)
        }

        val bodyStringCheat = Await.result(bodyStringFuture, 5.seconds)
        println(bodyStringCheat)
    }

    def requestControllerNewGameFieldGui(x: Option[String]): Unit = {
        val url = "http://controller:9081/controller/newGameFieldGui" + x.get // N-I-U
        
        val request = HttpRequest(
            method = HttpMethods.GET,
            uri = url
        )
    }

    def requestControllerSaveTime(currentTime: Int): Unit = {
        val url = "http://controller:9081/controller/saveTime/" + currentTime.toString // C
        
        val request = HttpRequest(
            method = HttpMethods.GET,
            uri = url
        )
    }

    def requestControllerSaveScoreAndPlayerName(score: Int, playerName: String): Unit = {
        val url = "http://controller:9081/controller/saveScoreAndPlayerName?score=" + score + "&playerName=" + playerName
        
        val request = HttpRequest(
            method = HttpMethods.GET,
            uri = url
        )

        val responseFuture: Future[HttpResponse] = Http().singleRequest(request)
        val bodyStringFuture: Future[String] = responseFuture.flatMap { response =>
            response.entity.toStrict(5.seconds).map(_.data.utf8String)
        }

        val bodyStringSave = Await.result(bodyStringFuture, 5.seconds)
        println(bodyStringSave)
    }

    def requestLoadPlayerScores: Seq[(String, Int)] = {
        val url = "http://persistence:9083/persistence/highscore"
        
        val request = HttpRequest(
            method = HttpMethods.GET,
            uri = url
        )
        
        val responseFuture: Future[HttpResponse] = Http().singleRequest(request)
        val bodyStringFuture: Future[String] = responseFuture.flatMap { response =>
            response.entity.toStrict(5.seconds).map(_.data.utf8String)
        }
        
        val jsonStringApiResult = Await.result(bodyStringFuture, 5.seconds)
        case class PlayerScore(player: String, score: Int)
        implicit val playerScoreFormat: Format[PlayerScore] = Json.format[PlayerScore]

        val json: JsValue = Json.parse(jsonStringApiResult)
        val playerScoresResult: JsResult[Seq[PlayerScore]] = Json.fromJson[Seq[PlayerScore]](json)

        val playerScores: Seq[(String, Int)] = playerScoresResult match {
            case JsSuccess(scores, _) => scores.map(ps => (ps.player, ps.score))
            case JsError(errors) => Seq.empty
        }

        playerScores
    }

    def requestControllerMakeAndPublishPut(move: Move) = {

        val url = "http://controller:9081/controller/makeAndPublish/put"
        val moveRaw = move

        def moveToJson(move: Move): JsValue = { Json.obj("x" -> move.x, "y" -> move.y, "value" -> move.value) }

        val bodyField = moveToJson(moveRaw)

        val request = HttpRequest(
            method = HttpMethods.GET,
            uri = url
        ).withEntity(HttpEntity(ContentTypes.`application/json`, bodyField.toString()))

        val bodyString = Await.result(Http().singleRequest(request).flatMap(_.entity.toStrict(5.seconds).map(_.data.utf8String)), 5.seconds)
    }

    def requestControllerMakeAndPublishDoMove(firstMoveCheck: Boolean, move: Move, game: GameTui) = {

        val newBoard = game.board match {
            case "Playing" => 0
            case "Won" => 1
            case "Lost" => 2
        }

        val url = s"http://controller:9081/controller/makeAndPublish/doMove?b=$firstMoveCheck&bombs=${game.bombs}&size=${game.side}&time=${game.time}&board=$newBoard"
        val moveRaw = move

        def moveToJson(move: Move): JsValue = { Json.obj("x" -> move.x, "y" -> move.y, "value" -> move.value) }

        val bodyField = moveToJson(moveRaw)

        val request = HttpRequest(
            method = HttpMethods.GET,
            uri = url
        ).withEntity(HttpEntity(ContentTypes.`application/json`, bodyField.toString()))

        val bodyString = Await.result(Http().singleRequest(request).flatMap(_.entity.toStrict(5.seconds).map(_.data.utf8String)), 5.seconds)
        println(bodyString)
    }

    def requestCheckGameOver(status: String) = {
        val url = "http://controller:9081/controller/checkGameOver"

        val request = HttpRequest(
            method = HttpMethods.PUT,
            uri = url
        ).withEntity(status)

        val responseFuture: Future[HttpResponse] = Http().singleRequest(request)
        val bodyStringFuture: Future[String] = responseFuture.flatMap { response =>
            response.entity.toStrict(5.seconds).map(_.data.utf8String)
        }
        val bodyString = Await.result(bodyStringFuture, 5.seconds)
        bodyString.toBoolean
    }

    def requestCheckGameOverGui = {
        val url = "http://controller:9081/controller/checkGameOverGui"

        val request = HttpRequest(
            method = HttpMethods.PUT,
            uri = url
        )

        val responseFuture: Future[HttpResponse] = Http().singleRequest(request)
        val bodyStringFuture: Future[String] = responseFuture.flatMap { response =>
            response.entity.toStrict(5.seconds).map(_.data.utf8String)
        }
        val bodyString = Await.result(bodyStringFuture, 5.seconds)
        bodyString.toBoolean
    }

    def requestGameOver = {

        val url = "http://controller:9081/controller/gameOver" // C

        val request = HttpRequest(
            method = HttpMethods.GET,
            uri = url
        )
        //val bodyString = Await.result(Http().singleRequest(request).flatMap(_.entity.toStrict(5.seconds).map(_.data.utf8String)), 5.seconds)
        //val field = jsonToFieldTui(bodyString)
    }
    
    def jsonToGameTui(jsonString: String): GameTui = {
        val json: JsValue = Json.parse(jsonString)
        val status = (json \ "game" \ "status").get.toString
        val bombs = (json \ "game" \ "bombs").get.toString.toInt
        val side = (json \ "game" \ "side").get.toString.toInt
        val time = (json \ "game" \ "time").get.toString.toInt
        val statusWithoutQuotes = status.replace("\"", "")
        GameTui(bombs, side, time, statusWithoutQuotes)
    }

    def jsonToFieldTui(jsonString: String): FieldTui = {
        val json: JsValue = Json.parse(jsonString)
        val size = (json \ "field" \ "size").get.toString.toInt

        val fieldVectorOption: Option[FieldTui] = Some(FieldTui(MatrixTui(Vector.tabulate(size, size) {(row, col) => "E"}), (MatrixTui(Vector.tabulate(size, size) {(row, col) => "E"}))))
        val matrixVectorOption: Option[Vector[Vector[String]]] = Some(Vector.tabulate(size, size) {(row, col) => "E"})
        val hiddenVectorOption: Option[Vector[Vector[String]]] = Some(Vector.tabulate(size, size) {(row, col) => "E"})

        val matrixVector1 = matrixVectorOption match{
            case Some(matrix) => matrix
            case None => println("Matrix is not valid"); Vector.tabulate(size, size) {(row, col) => "E"}
        }

        val updatedMatrixVector: Vector[Vector[String]] = (0 until size * size).foldLeft(matrixVector1) {
            case (currentMatrix, index) =>
                val row = (json \ "field" \ "matrix" \\ "row")(index).as[Int]
                val col = (json \ "field" \ "matrix" \\ "col")(index).as[Int]
                val cell = (json \ "field" \ "matrix" \\ "cell")(index).as[String]
                currentMatrix.updated(row, currentMatrix(row).updated(col, cell))
        }

        val hiddenVector1 = hiddenVectorOption match{
            case Some(m) => m
            case None => println("Hidden is not valid"); Vector.tabulate(size, size) {(row, col) => "E"}
        }
        
        val updatedHiddenVector: Vector[Vector[String]] = (0 until size * size).foldLeft(hiddenVector1) {
            case (currentHidden, index) =>
                val row = (json \ "field" \ "hidden" \\ "row")(index).as[Int]
                val col = (json \ "field" \ "hidden" \\ "col")(index).as[Int]
                val cell = (json \ "field" \ "hidden" \\ "cell")(index).as[String]
                currentHidden.updated(row, currentHidden(row).updated(col, cell))
        }

        val finalFieldOption = fieldVectorOption match{
            case Some(f) => Some(FieldTui(MatrixTui(updatedMatrixVector), MatrixTui(updatedHiddenVector)))
            case None => println("Field is not valid"); None
        }

        finalFieldOption.get
    }
    
}
