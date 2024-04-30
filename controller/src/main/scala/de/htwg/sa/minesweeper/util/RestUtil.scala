package de.htwg.sa.minesweeper.util

import de.htwg.sa.minesweeper.model.gameComponent.IField
import play.api.libs.json.JsValue
import play.api.libs.json.Json
import de.htwg.sa.minesweeper.model.gameComponent.gameBaseImpl.Field
import de.htwg.sa.minesweeper.model.gameComponent.gameBaseImpl.Matrix
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
/* import de.htwg.sa.minesweeper.entity.Field
import de.htwg.sa.minesweeper.entity.Matrix */

object RestUtil{

    implicit val system: ActorSystem = ActorSystem()
    implicit val materializer: Materializer = Materializer(system)
    implicit val executionContext: ExecutionContextExecutor = system.dispatcher
    
    def requestShowInvisibleCell(x: Int, y: Int, field: IField): String = {
        //field.showInvisibleCell(move.y, move.x)
        field.showInvisibleCell(x, y)
/*         import system.dispatcher // to get an execution context

        //val jasonField = field.fieldToJson// TODO: replace fieldToJson with own function for that in Controller
        val jasonField = field.fieldToJson(field)
        val jsonFileContent = jasonField.getBytes("UTF-8")

        val request2 = HttpRequest(
            method =  HttpMethods.PUT,
            uri = s"http://localhost:8082/model/field/showInvisibleCell?y=${y}&x=${x}",
            entity = HttpEntity(ContentTypes.`application/json`, jsonFileContent)
        )

        val responseFuture: Future[HttpResponse] = Http().singleRequest(request2)

        val bodyStringFuture: Future[String] = responseFuture.flatMap { response =>
            response.entity.toStrict(5.seconds).map(_.data.utf8String)
        }

        var symbol = "E"

        bodyStringFuture.onComplete {
            case Success(bodyString) =>
                symbol = bodyString
                val ANSI_PURPLE = "\u001B[35m"
                val ANSI_RESET = "\u001B[0m"

                println(ANSI_PURPLE + symbol + ANSI_RESET)
            case Failure(ex) =>
                sys.error(s"something wrong: ${ex.getMessage}")
        }

        val result = Await.result(bodyStringFuture, 5.seconds)
        result */
    }
        

    //field.put(extractedSymbol, move.y, move.x)
    def requestFieldPut(extractedSymbol: String, x: Int, y: Int, field: IField): IField = {
        //field.put(extractedSymbol, move.y, move.x)
        val AINSI_PINK = "\u001B[35m"
        val AINSI_RESET = "\u001B[0m"
        println(   AINSI_PINK + s"$extractedSymbol <- Track it" + AINSI_RESET)
        field.put(extractedSymbol, x, y)
/* 
        import system.dispatcher // to get an execution context

        val jasonField = field.fieldToJson(field) // TODO: replace fieldToJson with own function for that in Controller
        val jsonFileContent = jasonField.getBytes("UTF-8")

        //val secureSymbol = if (extractedSymbol == " "){"-"} else if (extractedSymbol == "*"){"b"} else extractedSymbol

        val ANSI_RED = "\u001B[31m"
        val ANSI_RESET = "\u001B[0m"
        println(ANSI_RED + extractedSymbol + " <- Track it"+ ANSI_RESET)

        val request2 = HttpRequest(
            method =  HttpMethods.PUT,
            uri = s"http://localhost:8082/model/field/put?symbol=${extractedSymbol}&x=${x}&y=${y}",
            entity = HttpEntity(ContentTypes.`application/json`, jsonFileContent)
        )

        val responseFuture: Future[HttpResponse] = Http().singleRequest(request2)

        val bodyFieldFuture: Future[String] = responseFuture.flatMap { response =>
            response.entity.toStrict(5.seconds).map(_.data.utf8String)
        }

        var jsonBodyField = "E"

        bodyFieldFuture.onComplete {
            case Success(bodyField) =>
                jsonBodyField = bodyField
                val ANSI_YELLOW = "\u001B[33m"
                val ANSI_RESET = "\u001B[0m"

                println(ANSI_YELLOW + jsonBodyField + ANSI_RESET)
            case Failure(ex) =>
                sys.error(s"something wrong: ${ex.getMessage}")
        }

        val result = Await.result(bodyFieldFuture, 5.seconds)
        jsonBodyField = result

        val fieldFromController = field.jsonToField(jsonBodyField)
        fieldFromController */
    }

    //field.recursiveOpen(move.x,move.y,field)
    def requestRecursiveOpen(x: Int, y: Int, field: IField): IField = field.recursiveOpen(x, y, field)
    
    
    // field.put("F", move.y, move.x)



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

}