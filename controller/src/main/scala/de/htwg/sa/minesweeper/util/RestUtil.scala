package de.htwg.sa.minesweeper.util

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.Materializer
import de.htwg.sa.minesweeper.entity.{FieldDTO, GameDTO, MatrixDTO}
import play.api.libs.json.{JsObject, JsValue, Json}

import scala.concurrent.{Await, ExecutionContextExecutor, Future}
import scala.concurrent.duration.*

object RestUtil{

    implicit val system: ActorSystem = ActorSystem()
    implicit val materializer: Materializer = Materializer(system)
    implicit val executionContext: ExecutionContextExecutor = system.dispatcher
    
    def requestShowInvisibleCell(x: Int, y: Int, field: FieldDTO): String = {
        import system.dispatcher

        val jasonField =  fieldDtoToJson(field)
        val jsonFileContent = jasonField.getBytes("UTF-8")

        // take care in the uri -> this is only valid for Command.scala replacement
        val request2 = HttpRequest(
            method =  HttpMethods.PUT,
            uri = s"http://model:9082/model/field/showInvisibleCell?y=${x}&x=${y}" ,
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
        
        import system.dispatcher

        val jasonField = fieldDtoToJson(field)
        val jsonFileContent = jasonField.getBytes("UTF-8")
        
        val extractedSymbolNew = if (extractedSymbol.length()>3) {"E"} else {extractedSymbol}

        val request2 = HttpRequest(
            method =  HttpMethods.PUT,
            uri = s"http://model:9082/model/field/put?symbol=$extractedSymbolNew&x=${x}&y=${y}",
            entity = HttpEntity(ContentTypes.`application/json`, jsonFileContent)
        )

        val responseFuture: Future[HttpResponse] = Http().singleRequest(request2)

        val bodyFieldFuture: Future[String] = responseFuture.flatMap { response =>
            response.entity.toStrict(5.seconds).map(_.data.utf8String)
        }

        var jsonBodyField = jasonField

        val result = Await.result(bodyFieldFuture, 5.seconds)
        jsonBodyField = result

        val fieldFromController = jsontToFieldDTO(jsonString = jsonBodyField )
        fieldFromController
        //Additionally check the field 
    }

    // approved
    def requestRecursiveOpen(x: Int, y: Int, field: FieldDTO): FieldDTO = 
    {
        // def recursiveOpen(x: Int, y: Int, field: IField): IField
        import system.dispatcher // to get an execution context
        val jasonField = fieldDtoToJson(field) // We need a request with a field in json format
        val jsonFileContent = jasonField.getBytes("UTF-8")
        // then we need to send x and y as a parameter
        val request = HttpRequest(
            method =  HttpMethods.PUT,
            uri = s"http://model:9082/model/field/recursiveOpen?x=${x}&y=${y}",
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

        (game, finalFieldOption.get)
    }
    
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

    def fieldDtoToJson(fieldInput: FieldDTO): String = {
        import play.api.libs.json.*
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