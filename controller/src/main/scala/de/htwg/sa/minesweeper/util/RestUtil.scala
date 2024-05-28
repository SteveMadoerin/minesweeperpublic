package de.htwg.sa.minesweeper.util

import play.api.libs.json.JsValue
import play.api.libs.json.Json
import play.api.libs.json.{JsValue, Json}
import scala.io.Source

import play.api.libs.json.JsValue
import play.api.libs.json.Json
import scala.io.Source
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.HttpMethods
import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.model.ContentTypes
import akka.http.scaladsl.Http
import scala.concurrent.Future
import akka.http.scaladsl.model.HttpResponse
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContextExecutor
import akka.actor.ActorSystem
import akka.stream.Materializer
import scala.concurrent.Await
import play.api.libs.json.JsObject
import play.api.libs.json.JsString
import scala.annotation.internal.Body
import akka.util.ByteString
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import scala.concurrent.Future
import scala.util.{Failure, Success}

import scala.util.{Try, Success, Failure}
import akka.http.scaladsl.unmarshalling.Unmarshal
import scala.concurrent.Future
import scala.concurrent.duration._
import de.htwg.sa.minesweeper.entity.FieldDTO
import de.htwg.sa.minesweeper.entity.MatrixDTO
import de.htwg.sa.minesweeper.entity.GameDTO

object RestUtil{

    implicit val system: ActorSystem = ActorSystem()
    implicit val materializer: Materializer = Materializer(system)
    implicit val executionContext: ExecutionContextExecutor = system.dispatcher
    
    def requestShowInvisibleCell(x: Int, y: Int, field: FieldDTO): String = {
        import system.dispatcher // to get an execution context

        val jasonField = /* fieldToJson(field) */ fieldDtoToJson(field)
        val jsonFileContent = jasonField.getBytes("UTF-8")

        // take care in the uri -> this is only valid for Command.scala replacement
        val request2 = HttpRequest(
            method =  HttpMethods.PUT,
            uri = s"http://localhost:9082/model/field/showInvisibleCell?y=${x}&x=${y}" ,
            entity = HttpEntity(ContentTypes.`application/json`, jsonFileContent)
        )

        val responseFuture: Future[HttpResponse] = Http().singleRequest(request2)
        val bodyStringFuture: Future[String] = responseFuture.flatMap { response =>
            response.entity.toStrict(5.seconds).map(_.data.utf8String)
        }

        val result = Await.result(bodyStringFuture, 5.seconds)
        
        if (result.length()>3) {"E"} else {result}
    }
    
    // approved
    def requestFieldPut(extractedSymbol: String, x: Int, y: Int, field: FieldDTO): FieldDTO = {

        // ________________________________________________________________________________________
        import system.dispatcher // to get an execution context

        val jasonField = /* fieldToJson(field) */fieldDtoToJson(field)
        val jsonFileContent = jasonField.getBytes("UTF-8")
        
        val extractedSymbolNew = if (extractedSymbol.length()>3) {"E"} else {extractedSymbol}

        val request2 = HttpRequest(
            method =  HttpMethods.PUT,
            uri = s"http://localhost:9082/model/field/put?symbol=$extractedSymbolNew&x=${x}&y=${y}",
            entity = HttpEntity(ContentTypes.`application/json`, jsonFileContent)
        )

        val responseFuture: Future[HttpResponse] = Http().singleRequest(request2)

        val bodyFieldFuture: Future[String] = responseFuture.flatMap { response =>
            response.entity.toStrict(5.seconds).map(_.data.utf8String)
        }

        var jsonBodyField = jasonField

        val result = Await.result(bodyFieldFuture, 5.seconds)
        jsonBodyField = result

        val fieldFromController = /* jsonToField(jsonBodyField) */jsontToFieldDTO(jsonString = jsonBodyField )
        fieldFromController
        //Additionally check the field
        // ________________________________________________________________________________________
    }

    // approved
    def requestRecursiveOpen(x: Int, y: Int, field: FieldDTO): FieldDTO = 
    {
        // def recursiveOpen(x: Int, y: Int, field: IField): IField
        import system.dispatcher // to get an execution context
        // We need a request with a field in json format
        val jasonField = /* fieldToJson(field) */ fieldDtoToJson(field)
        val jsonFileContent = jasonField.getBytes("UTF-8")
        // then we need to send x and y as a parameter
        val request = HttpRequest(
            method =  HttpMethods.PUT,
            uri = s"http://localhost:9082/model/field/recursiveOpen?x=${x}&y=${y}",
            entity = HttpEntity(ContentTypes.`application/json`, jsonFileContent)
        )

        val responseFuture: Future[HttpResponse] = Http().singleRequest(request)
        val bodyFieldFuture: Future[String] = responseFuture.flatMap { response =>
            response.entity.toStrict(5.seconds).map(_.data.utf8String)
        }
        val result = Await.result(bodyFieldFuture, 5.seconds)
        val fieldFromController = jsontToFieldDTO(result)
        fieldFromController
        
    }
    
    def jsonToGameDTO(jsonString: String): GameDTO = {
        val json: JsValue = Json.parse(jsonString)
        val status = (json \ "game" \ "status").get.toString
        val bombs = (json \ "game" \ "bombs").get.toString.toInt
        val side = (json \ "game" \ "side").get.toString.toInt
        val time = (json \ "game" \ "time").get.toString.toInt
        val statusWithoutQuotes = status.replace("\"", "") // \Playing\ -> Playing
        GameDTO(bombs, side, time, statusWithoutQuotes)
    }
    
    
/*     def jsonToGame(jsonString: String): IGame = {
        val json: JsValue = Json.parse(jsonString)
        val status = (json \ "game" \ "status").get.toString
        val bombs = (json \ "game" \ "bombs").get.toString.toInt
        val side = (json \ "game" \ "side").get.toString.toInt
        val time = (json \ "game" \ "time").get.toString.toInt
        val statusWithoutQuotes = status.replace("\"", "") // \Playing\ -> Playing
        Game(bombs, side, time, statusWithoutQuotes)
    } */

/*     def gameToJson(currentGame: IGame): String = {
        Json.prettyPrint(
            Json.obj(
                "game" -> Json.obj(
                    "status" -> currentGame.board,
                    "bombs" -> currentGame.bombs,
                    "side" -> currentGame.side,
                    "time" -> currentGame.time
                )
            )
        )
    } */

    def gameDtoToJson(currentGame: GameDTO): String = {
        Json.prettyPrint(
            Json.obj(
                "game" -> Json.obj(
                    "status" -> currentGame.board,
                    "bombs" -> currentGame.bombs,
                    "side" -> currentGame.side,
                    "time" -> currentGame.time
                )
            )
        )
    }

    def jsonToGameAndFieldDTO(jsonString: String): (GameDTO, FieldDTO) = {
        val json: JsValue = Json.parse(jsonString)
        val jsonGame: Option[JsValue] = (json \\ "game").headOption
        val status = (jsonGame.get \ "status").get.toString
        val bombs = (jsonGame.get \ "bombs").get.toString.toInt
        val side = (jsonGame.get \ "side").get.toString.toInt
        val time = (jsonGame.get \ "time").get.toString.toInt
        val statusWithoutQuotes = status.replace("\"", "")
        val game = GameDTO(bombs, side, time, statusWithoutQuotes)

        val jsonField: Option[JsValue] = (json \\ "field").headOption

        val jsonValue = jsonField.get

        val size = (jsonValue \ "size").get.toString.toInt

        val fieldVectorOption: Option[FieldDTO] = Some(FieldDTO(MatrixDTO(Vector.tabulate(size, size) {(row, col) => "E"}), (MatrixDTO(Vector.tabulate(size, size) {(row, col) => "E"}))))
        val matrixVectorOption: Option[Vector[Vector[String]]] = Some(Vector.tabulate(size, size) {(row, col) => "E"})
        val hiddenVectorOption: Option[Vector[Vector[String]]] = Some(Vector.tabulate(size, size) {(row, col) => "E"})

        val matrixVector1 = matrixVectorOption match{
            case Some(matrix) => matrix
            case None => println("Matrix is not valid"); Vector.tabulate(size, size) {(row, col) => "E"}
        }

        val updatedMatrixVector: Vector[Vector[String]] = (0 until size * size).foldLeft(matrixVector1) {
            case (currentMatrix, index) =>
                val row = (jsonValue \ "matrix" \\ "row")(index).as[Int]
                val col = (jsonValue \ "matrix" \\ "col")(index).as[Int]
                val cell = (jsonValue \ "matrix" \\ "cell")(index).as[String]
                currentMatrix.updated(row, currentMatrix(row).updated(col, cell))
        }

        val hiddenVector1 = hiddenVectorOption match{
            case Some(m) => m
            case None => println("Hidden is not valid"); Vector.tabulate(size, size) {(row, col) => "E"}
        }
        
        val updatedHiddenVector: Vector[Vector[String]] = (0 until size * size).foldLeft(hiddenVector1) {
            case (currentHidden, index) =>
                val row = (jsonValue \ "hidden" \\ "row")(index).as[Int]
                val col = (jsonValue \ "hidden" \\ "col")(index).as[Int]
                val cell = (jsonValue \ "hidden" \\ "cell")(index).as[String]
                currentHidden.updated(row, currentHidden(row).updated(col, cell))
        }

        val finalFieldOption = fieldVectorOption match{
            case Some(f) => Some(FieldDTO(MatrixDTO(updatedMatrixVector), MatrixDTO(updatedHiddenVector)))
            case None => println("Field is not valid"); None
        }

        (game, finalFieldOption.get) //finalFieldOption.get
    }

    /* def jsonToGameAndField(jsonString: String): (IGame, IField) = {
        val json: JsValue = Json.parse(jsonString)
        val jsonGame: Option[JsValue] = (json \\ "game").headOption
        val status = (jsonGame.get \ "status").get.toString
        val bombs = (jsonGame.get \ "bombs").get.toString.toInt
        val side = (jsonGame.get \ "side").get.toString.toInt
        val time = (jsonGame.get \ "time").get.toString.toInt
        val statusWithoutQuotes = status.replace("\"", "")
        val game = Game(0, 0, 0, "").insertBomb(bombs).insertSide(side).insertTime(time).insertBoard(statusWithoutQuotes)

        val jsonField: Option[JsValue] = (json \\ "field").headOption

        val jsonValue = jsonField.get

        val size = (jsonValue \ "size").get.toString.toInt

        val fieldVectorOption: Option[IField] = Some(Field(Matrix(Vector.tabulate(size, size) {(row, col) => "E"}), (Matrix(Vector.tabulate(size, size) {(row, col) => "E"}))))
        val matrixVectorOption: Option[Vector[Vector[String]]] = Some(Vector.tabulate(size, size) {(row, col) => "E"})
        val hiddenVectorOption: Option[Vector[Vector[String]]] = Some(Vector.tabulate(size, size) {(row, col) => "E"})

        val matrixVector1 = matrixVectorOption match{
            case Some(matrix) => matrix
            case None => println("Matrix is not valid"); Vector.tabulate(size, size) {(row, col) => "E"}
        }

        val updatedMatrixVector: Vector[Vector[String]] = (0 until size * size).foldLeft(matrixVector1) {
            case (currentMatrix, index) =>
                val row = (jsonValue \ "matrix" \\ "row")(index).as[Int]
                val col = (jsonValue \ "matrix" \\ "col")(index).as[Int]
                val cell = (jsonValue \ "matrix" \\ "cell")(index).as[String]
                currentMatrix.updated(row, currentMatrix(row).updated(col, cell))
        }

        val hiddenVector1 = hiddenVectorOption match{
            case Some(m) => m
            case None => println("Hidden is not valid"); Vector.tabulate(size, size) {(row, col) => "E"}
        }
        
        val updatedHiddenVector: Vector[Vector[String]] = (0 until size * size).foldLeft(hiddenVector1) {
            case (currentHidden, index) =>
                val row = (jsonValue \ "hidden" \\ "row")(index).as[Int]
                val col = (jsonValue \ "hidden" \\ "col")(index).as[Int]
                val cell = (jsonValue \ "hidden" \\ "cell")(index).as[String]
                currentHidden.updated(row, currentHidden(row).updated(col, cell))
        }

        val finalFieldOption = fieldVectorOption match{
            case Some(f) => Some(Field(Matrix(updatedMatrixVector), Matrix(updatedHiddenVector)))
            case None => println("Field is not valid"); None
        }

        (game, finalFieldOption.get) //finalFieldOption.get
    } */


    def jsontToFieldDTO(jsonString: String): FieldDTO = {
        val json: JsValue = Json.parse(jsonString)
        val size = (json \ "field" \ "size").get.toString.toInt

        val fieldVectorOption: Option[FieldDTO] = Some(FieldDTO(MatrixDTO(Vector.tabulate(size, size) {(row, col) => "E"}), (MatrixDTO(Vector.tabulate(size, size) {(row, col) => "E"}))))
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
            case Some(f) => Some(FieldDTO(MatrixDTO(updatedMatrixVector), MatrixDTO(updatedHiddenVector)))
            case None => println("Field is not valid"); None
        }

        finalFieldOption.get
    }
/* 
    def jsonToField(jsonString: String): IField = {
        // T0DO: Replace Field gameComponent with Field Controller

        val json: JsValue = Json.parse(jsonString)
        val size = (json \ "field" \ "size").get.toString.toInt

        val fieldVectorOption: Option[IField] = Some(Field(Matrix(Vector.tabulate(size, size) {(row, col) => "E"}), (Matrix(Vector.tabulate(size, size) {(row, col) => "E"}))))
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
            case Some(f) => Some(Field(Matrix(updatedMatrixVector), Matrix(updatedHiddenVector)))
            case None => println("Field is not valid"); None
        }

        finalFieldOption.get
    } */

/*     def fieldToJson(fieldInput: IField): String = {
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
    } */

    def fieldDtoToJson(fieldInput: FieldDTO): String = {
        import play.api.libs.json._
        Json.prettyPrint(
            Json.obj(
                "field" -> Json.obj(
                    "size" -> fieldInput.matrix.rows.size,
                    "matrix" -> Json.toJson(
                        for {
                            row <- 0 until fieldInput.matrix.rows.size
                            col <- 0 until fieldInput.matrix.rows.size
                        } yield {
                            Json.obj(
                                "row" -> row,
                                "col" -> col,
                                "cell" -> fieldInput.matrix.rows(row)(col).toString // def cell(row: Int, col: Int): T = rows(row)(col)
                            )
                        }
                    ),
                    "hidden" -> Json.toJson(
                        for {
                            row <- 0 until fieldInput.hidden.rows.size
                            col <- 0 until fieldInput.hidden.rows.size
                        } yield {
                            Json.obj(
                                "row" -> row,
                                "col" -> col,
                                "cell" -> fieldInput.hidden.rows(row)(col).toString //fieldInput.showInvisibleCell(row, col).toString
                            )
                        }
                    )
                )
            )
        )
    }

}