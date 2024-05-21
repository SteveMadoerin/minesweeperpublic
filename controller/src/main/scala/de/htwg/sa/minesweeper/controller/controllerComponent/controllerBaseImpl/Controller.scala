package de.htwg.sa.minesweeper.controller.controllerComponent.controllerBaseImpl

import de.htwg.sa.minesweeper.controller.controllerComponent.config.Default
import de.htwg.sa.minesweeper.controller.controllerComponent.IController
/* import de.htwg.sa.minesweeper.model.gameComponent.gameBaseImpl._
import de.htwg.sa.minesweeper.model.gameComponent._ */
/* import de.htwg.sa.minesweeper.persistence.fileIoComponent.IFileIO */
import de.htwg.sa.minesweeper.util.{Observable, Move, UndoRedoManager, Event}

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
import scala.annotation.internal.Body
import akka.util.ByteString
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer


import scala.util.{Try, Success, Failure}
import akka.http.scaladsl.unmarshalling.Unmarshal
import scala.concurrent.Future
import scala.concurrent.duration._
import de.htwg.sa.minesweeper.util.RestUtil
import play.api.libs.json.{JsArray, JsValue, Json, JsObject, JsString, JsResult, JsSuccess, JsError, Format}

import java.nio.file.{Files, Paths}
import scala.util.{Failure, Success}
import de.htwg.sa.minesweeper.entity.{FieldDTO, GameDTO, MatrixDTO}




class Controller() extends IController with Observable:
    
    
    implicit val system: ActorSystem = ActorSystem()
    implicit val materializer: Materializer = Materializer(system)
    implicit val executionContext: ExecutionContextExecutor = system.dispatcher
    
    var game: GameDTO = GameDTO(10, 9, 0, "Playing")
    var field: FieldDTO = createFieldDTO(game)

    val undoRedoManager = new UndoRedoManager[FieldDTO]

    def createFieldDTO(leGame: GameDTO): FieldDTO = {
        val url = s"http://localhost:9082/model/field/new?bombs=${leGame.bombs}&size=${leGame.side}&time=${leGame.time}"

        val returnField: FieldDTO = Try {
            val result = Source.fromURL(url).mkString
            /* RestUtil.jsonToField(result) */
            RestUtil.jsontToFieldDTO(result)
        } match {
            case Success(field) => field
            case Failure(e) =>
                println(s"Error fetching or parsing field data: ${e.getMessage}")
                FieldDTO(MatrixDTO[String](Vector.empty), new MatrixDTO[String](Vector.empty)) // Return a default field or handle the error appropriately
            
        }
        returnField
    }


    def doMove(b: Boolean, move: Move, game: GameDTO) = 
        synchronized{            

            val convertedBoard = game.board match {
                case "Playing" => 0
                case "Won" => 1
                case "Lost" => 2
            }
            // ModelAPI call: model/field/decider with parameters b, x, y, bombs size, time, board
            val url = s"http://localhost:9082/model/field/decider?b=${b}&x=${move.x}&y=${move.y}&bombs=${game.bombs}&size=${game.side}&time=${game.time}&board=$convertedBoard"
            val jsonFieldFirst = RestUtil.fieldDtoToJson(field)/* RestUtil.fieldToJson(field) */ // provide the controller field to the ModelAPI
            val jsonFileContentFirst = jsonFieldFirst.getBytes("UTF-8")

            val firstRequest = requestPut(url, jsonFileContentFirst) // generate put request with the controller field in body

            val firstResponseFuture: Future[HttpResponse] = Http().singleRequest(firstRequest) // get response
            val firstBodyStringFuture: Future[String] = firstResponseFuture.flatMap { response =>
                response.entity.toStrict(5.seconds).map(_.data.utf8String)
            }

            val returnedJsonGameAndField = Await.result(firstBodyStringFuture, 5.seconds)
            val (newGame, newField): (GameDTO, FieldDTO) = RestUtil.jsonToGameAndFieldDTO(returnedJsonGameAndField) // JsonArray with Game and Field converting
            field = newField
            this.game = newGame
            
            val jasonField2 = RestUtil.fieldDtoToJson(field)
            val jsonFileContent2 = jasonField2.getBytes("UTF-8")

            val request2 = HttpRequest(
                method =  HttpMethods.PUT,
                uri = s"http://localhost:9082/model/field/showInvisibleCell?y=${move.y}&x=${move.x}",
                entity = HttpEntity(ContentTypes.`application/json`, jsonFileContent2)
            )

            val responseFuture: Future[HttpResponse] = Http().singleRequest(request2)
            val bodyStringFuture: Future[String] = responseFuture.flatMap { response =>
                response.entity.toStrict(5.seconds).map(_.data.utf8String)
            }

            val result = Await.result(bodyStringFuture, 5.seconds)
            val symbol = if (result.length()>3) {"E"} else {result}

            if(symbol == "E"){field}
            else if(symbol == "0"){field}
            else{undoRedoManager.doStep(field, DoCommand(move))}
        }
    
    def loadGame = {

        val uri = Uri("http://localhost:9083/persistence/game") // load the game
        val request = HttpRequest(method = HttpMethods.GET, uri = uri)
        val responseFuture: Future[HttpResponse] = Http().singleRequest(request)
        val bodyStringFuture: Future[String] = responseFuture.flatMap { response =>
            response.entity.toStrict(5.seconds).map(_.data.utf8String)
        }
        val result = Await.result(bodyStringFuture, 5.seconds)

        // now we need to parse the resulting jsong string representig the Game
        val loadedGame: Option[GameDTO] = Try {
            RestUtil.jsonToGameDTO(result)
        } match {
            case Success(game) => println("game loaded"); Some(game)
            case Failure(e) => None
        }

        val gameOption = loadedGame
        gameOption match {
            case Some(game) => this.game = game
            case None =>
        }
        game = GameDTO(gameOption.get.bombs, gameOption.get.side, gameOption.get.time, gameOption.get.board)
        // _____________________________________________________________________________________________________
        
        val uriField = Uri("http://localhost:9083/persistence/field") // load the field

        val requestField = HttpRequest(method = HttpMethods.GET, uri = uriField)
        val responseFutureField: Future[HttpResponse] = Http().singleRequest(requestField)
        val bodyStringFutureField: Future[String] = responseFutureField.flatMap { response =>
            response.entity.toStrict(5.seconds).map(_.data.utf8String)
        }
        val resultField = Await.result(bodyStringFutureField, 5.seconds)
        val loadedField: Option[FieldDTO] = Try {
            RestUtil.jsontToFieldDTO(resultField)
        } match {
            case Success(field) => println("field loaded"); Some(field)
            case Failure(e) => None
        }

        val fieldOption: Option[FieldDTO] = loadedField
        fieldOption match {
            case Some(field) => this.field = field
            case None =>
        }

        notifyObserversRest("Load")
    }

    def saveGame =

        notifyObserversRest("SaveTime")

        val queryParameters = Uri.Query(
            "bombs" -> s"${game.bombs}",
            "size"  -> s"${game.side}",
            "time"  -> s"${game.time}",
            "board" -> s"${game.board}"
        )

        val uri = Uri("http://localhost:9083/persistence/putGame").withQuery(queryParameters)
        val request = HttpRequest(method = HttpMethods.PUT, uri = uri)

        val responseFuture: Future[HttpResponse] = Http().singleRequest(request)

        responseFuture.onComplete {
            case Success(res) => println(s"Request successful: $res")
            case Failure(ex)  => println(s"Request failed: $ex")
        }

        // TODO: Await for the response

        // _____________________________________________________________________________________________________

        val jasonField = RestUtil.fieldDtoToJson(field)
        val jsonFileContent = jasonField.getBytes("UTF-8")

        val request2 = HttpRequest(
            method =  HttpMethods.PUT,
            uri = "http://localhost:9083/persistence/putField",
            entity = HttpEntity(ContentTypes.`application/json`, jsonFileContent)
        )

        val responseFuture2: Future[HttpResponse] = Http().singleRequest(request2)
        responseFuture2.onComplete {
            case Success(res) => println(s"Successfully saved JSON file field: ${res.entity}")
            case Failure(ex)  => println(s"Failed to put JSON file: ${ex.getMessage}")
        }

        game = GameDTO(game.bombs, game.side, game.time, "Playing") // TODO: game.time is not updated
        notifyObserversRest("Save") //notifyObservers(Event.Save)
    
    def exit = notifyObserversRest("Exit")

    // approved
    def gameOver =
        val url = s"http://localhost:9082/model/field/gameOverField"
        val jsonField =  RestUtil.fieldDtoToJson(field) // provide the controller field to the ModelAPI
        val jsonFileContent = jsonField.getBytes("UTF-8")
        
        val request = HttpRequest(
            method =  HttpMethods.PUT,
            uri = url,
            entity = HttpEntity(ContentTypes.`application/json`, jsonFileContent)
        )
        val responseFuture: Future[HttpResponse] = Http().singleRequest(request)
        val bodyStringFuture: Future[String] = responseFuture.flatMap { response =>
            response.entity.toStrict(5.seconds).map(_.data.utf8String)
        }
        val bodyString = Await.result(bodyStringFuture, 5.seconds)
        field = RestUtil.jsontToFieldDTO(bodyString)

        notifyObserversRest("GameOver")
    
    def openRec(x: Int, y: Int, field: FieldDTO): FieldDTO = undoRedoManager.doStep(field, DoCommand(Move("recursion", x, y))) // approved

    def helpMenu = 
        helpMenuRest
        notifyObserversRest("Help")
    
    def fieldToString: String = {
        val url2 = s"http://localhost:9082/model/field/toString"
        val bodyField = RestUtil.fieldDtoToJson(field)

        val request2 = HttpRequest(
            method =  HttpMethods.PUT,
            uri = url2,
            entity = HttpEntity(ContentTypes.`application/json`, bodyField)
        )

        val responseFuture2: Future[HttpResponse] = Http().singleRequest(request2) 
        val bodyStringFuture2: Future[String] = responseFuture2.flatMap { response =>
            response.entity.toStrict(5.seconds).map(_.data.utf8String)
        }
        val bodyString2 = Await.result(bodyStringFuture2, 5.seconds) // now get the printed field from the modelApi
        bodyString2
    }

    def helpMenuRest: String = 
        val url = s"http://localhost:9082/model/game/helpMessage"
        // prepare the GET request
        val request = HttpRequest(
            method =  HttpMethods.GET,
            uri = url
        )

        val responseFuture: Future[HttpResponse] = Http().singleRequest(request) // now get the message from the modelApi
        val bodyStringFuture: Future[String] = responseFuture.flatMap { response =>
            response.entity.toStrict(5.seconds).map(_.data.utf8String)
        }
        val bodyStringHelpMessage = Await.result(bodyStringFuture, 5.seconds)
        bodyStringHelpMessage
    
    def cheat = 
        cheatRest
        notifyObserversRest("Cheat")
    
    def cheatRest: String = {
        val url = s"http://localhost:9082/model/field/cheat"
        
        val bodyField = RestUtil.fieldDtoToJson(field) // prepare the Field

        val request = HttpRequest(
            method =  HttpMethods.PUT,
            uri = url,
            entity = HttpEntity(ContentTypes.`application/json`, bodyField)
        )

        val responseFuture: Future[HttpResponse] = Http().singleRequest(request) // now get the printed field from the modelApi
        val bodyStringFuture: Future[String] = responseFuture.flatMap { response =>
            response.entity.toStrict(5.seconds).map(_.data.utf8String)
        }
        val bodyString = Await.result(bodyStringFuture, 5.seconds)
        bodyString
    }

    def checkGameOver(status: String) = {
        val url = s"http://localhost:9082/model/game/checkExit?board=$status"
        val request = HttpRequest(
            method =  HttpMethods.GET,
            uri = url
        )
        val responseFuture: Future[HttpResponse] = Http().singleRequest(request)
        val bodyStringFuture: Future[String] = responseFuture.flatMap { response =>
            response.entity.toStrict(5.seconds).map(_.data.utf8String)
        }
        val bodyString = Await.result(bodyStringFuture, 5.seconds)
        bodyString.toBoolean // parse String to boolean
    }

    def newGameGUI =
        game = GameDTO(game.bombs, game.side, game.time, "Playing")
        notifyObserversRest("Input") //notifyObservers(Event.Input)
    
    // approved
    def newGameField(optionString: Option[String]) =
        game = GameDTO(game.bombs, game.side, game.time, "Playing")

        val oString = optionString.get match {
            case "SuperEasy" => 0
            case "Easy" => 1 
            case "Medium" => 2
            case "Hard" => 3
        }
        
        val url = s"http://localhost:9082/model/game/newGameField?optionString=$oString"

        val bodyGame = /* RestUtil.gameToJson(game) */ RestUtil.gameDtoToJson(game) // prepare the game
        val body = bodyGame.getBytes("UTF-8")

        val request = requestPut(url, body)  // prepare the PUT request

        val responseFuture: Future[HttpResponse] = Http().singleRequest(request)
        val bodyStringFuture: Future[String] = responseFuture.flatMap { response =>
            response.entity.toStrict(5.seconds).map(_.data.utf8String)
        }
        val returnedJsonGameAndField = Await.result(bodyStringFuture, 5.seconds)
        val (newGame, newField): (GameDTO, FieldDTO) = RestUtil.jsonToGameAndFieldDTO(returnedJsonGameAndField)
        field = newField                
        game = GameDTO(newGame.bombs, newGame.side, newGame.time, newGame.board) 
        notifyObserversRest("NewGame") //notifyObservers(Event.NewGame)
    
    def newGame(side: Int, bombs: Int) =
        game = GameDTO(bombs, side, game.time, "Playing")
        field = createFieldDTO(game)
        notifyObserversRest("NewGame")
    
    // approved - doMove from TUI transfered as param -> game & field in TUI declared
    def makeAndPublish(makeThis: (Boolean, Move, GameDTO) => FieldDTO, b: Boolean, move: Move, game: GameDTO): Unit =
        field = makeThis(b, move, game)  // doMove
        
        val jasonField = RestUtil.fieldDtoToJson(field)
        val jsonFileContent = jasonField.getBytes("UTF-8")
        val request = requestPut(s"http://localhost:9082/model/field/showInvisibleCell?y=${move.y}&x=${move.x}", jsonFileContent) // -> request to ModelAPi
        val responseFuture: Future[HttpResponse] = Http().singleRequest(request)
        val bodyStringFuture: Future[String] = responseFuture.flatMap { response =>
            response.entity.toStrict(5.seconds).map(_.data.utf8String)
        }

        val result = Await.result(bodyStringFuture, 5.seconds)
        val symbol = if(result.length > 3){"E"} else {result}

        if (symbol == "0"){field = openRec(move.x,move.y,field)}
        val firstOrNext = if (b) "Start" else "Next"
        notifyObserversRest(firstOrNext)
    
    def makeAndPublish(makeThis: Move => FieldDTO, move: Move): Unit =
        move.value match {
            case "flag" => 
                val jasonField = RestUtil.fieldDtoToJson(field)
                val jsonFileContent = jasonField.getBytes("UTF-8")

                val request = HttpRequest(
                    method =  HttpMethods.PUT,
                    uri = s"http://localhost:9082/model/field/put/flag?x=${move.x}&y=${move.y}",
                    entity = HttpEntity(ContentTypes.`application/json`, jsonFileContent)
                )

                val responseFuture: Future[HttpResponse] = Http().singleRequest(request)
                val bodyFieldFuture: Future[String] = responseFuture.flatMap { response =>
                    response.entity.toStrict(5.seconds).map(_.data.utf8String)
                }

                var jsonBodyField = "E"
                bodyFieldFuture.onComplete {
                    case Success(bodyField) =>
                        jsonBodyField = bodyField
                    case Failure(ex) =>
                        sys.error(s"something wrong: ${ex.getMessage}")
                }

                val result = Await.result(bodyFieldFuture, 5.seconds)
                jsonBodyField = result

                val fieldFromController = RestUtil.jsontToFieldDTO(jsonBodyField)
                field = fieldFromController

            case "unflag" => 
                val jasonField = RestUtil.fieldDtoToJson(field)
                val jsonFileContent = jasonField.getBytes("UTF-8")

                val request2 = HttpRequest(
                    method =  HttpMethods.PUT,
                    uri = s"http://localhost:9082/model/field/put/removeFlag?x=${move.x}&y=${move.y}",
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
                    case Failure(ex) =>
                        sys.error(s"something wrong: ${ex.getMessage}")
                }

                val result = Await.result(bodyFieldFuture, 5.seconds)
                jsonBodyField = result

                val fieldFromController = RestUtil.jsontToFieldDTO(jsonBodyField)
                field = fieldFromController
        }

        field = makeThis(move)
        notifyObserversRest("Next")
    

    def makeAndPublish(makeThis: => FieldDTO): FieldDTO =
        field = makeThis
        notifyObserversRest("Next")
        field

    
    
    // approved
    def saveScoreAndPlayerName(playerName: String, saveScore: Int, filePath: String) = {

        val queryParameters = Uri.Query(
            "player" -> s"${playerName}",
            "score"  -> s"${saveScore}"
        )

        val uri = Uri("http://localhost:9083/persistence/putHighscore").withQuery(queryParameters)
        val request = HttpRequest(method = HttpMethods.PUT, uri = uri)

        val responseFuture: Future[HttpResponse] = Http().singleRequest(request)

        responseFuture.onComplete {
        case Success(res) => println(s"Request successful: $res")
        case Failure(ex)  => println(s"Request failed: $ex")
        }
    }

    def loadPlayerScores: Seq[(String, Int)] = {
        
        val url = "http://localhost:9083/persistence/highscore"
        val request = HttpRequest(method = HttpMethods.GET, uri = url)
        val responseFuture: Future[HttpResponse] = Http().singleRequest(request)
        val bodyFuture: Future[String] = responseFuture.flatMap { response =>
            response.entity.toStrict(5.seconds).map(_.data.utf8String)
        }
        val jsonStringApiResult: String = Await.result(bodyFuture, 5.seconds)
        
        case class PlayerScore(player: String, score: Int) // body needs to be convertoed to a Seq[(String, Int)]
        implicit val playerScoreFormat: Format[PlayerScore] = Json.format[PlayerScore]
        
        val json: JsValue = Json.parse(jsonStringApiResult) // Parse the JSON string into a sequence of PlayerScore objects
        val playerScoresResult: JsResult[Seq[PlayerScore]] = Json.fromJson[Seq[PlayerScore]](json)

        val playerScores: Seq[(String, Int)] = playerScoresResult match {
        case JsSuccess(scores, _) => scores.map(ps => (ps.player, ps.score))
        case JsError(errors) => Seq.empty // Handle the error appropriately
        } // Map the sequence of PlayerScore objects to the desired Seq[(String, Int)]

        playerScores
    }
    
    def put(move: Move): FieldDTO = undoRedoManager.doStep(field, DoCommand(move))
    def undo: FieldDTO = undoRedoManager.undoStep(field)
    def redo: FieldDTO = undoRedoManager.redoStep(field)
    def saveTime(currentTime: Int): Unit = 
        val tempGame: GameDTO = GameDTO(game.bombs, game.side, currentTime, "Playing")
        game = tempGame
    
    def requestPut(input_uri: String, jsonContent: Array[Byte]) = {
        HttpRequest(
            method =  HttpMethods.PUT,
            uri = input_uri,
            entity = HttpEntity(ContentTypes.`application/json`, jsonContent)
        )
    }

    // approved - but only to notify the TUI and GUI
    def notifyObserversRest(event: String) = {
        // TODO: maybe register the logic

        // _________________________ NOTIFY TUI _________________________

        val uri1 = s"http://localhost:9088/tui/notify" + "?event=" + event

        val request = HttpRequest(
            method =  HttpMethods.PUT,
            uri = uri1
        )
        
        val responseFuture: Future[HttpResponse] = Http().singleRequest(request)
        val bodyFieldFuture: Future[String] = responseFuture.flatMap { response =>
            response.entity.toStrict(5.seconds).map(_.data.utf8String)
        }
        val result = Await.result(bodyFieldFuture, 5.seconds)
        println(result + " - TUI")

        // _________________________ NOTIFY GUI _________________________
        val uri2 = s"http://localhost:9087/gui/notify" + "?event=" + event

        val request2 = HttpRequest(
            method =  HttpMethods.PUT,
            uri = uri2
        )
        
        val responseFuture2: Future[HttpResponse] = Http().singleRequest(request2)
        val bodyFieldFuture2: Future[String] = responseFuture2.flatMap { response =>
            response.entity.toStrict(5.seconds).map(_.data.utf8String)
        }
        val result2 = Await.result(bodyFieldFuture2, 5.seconds)
        println(result2 + " - GUI")
    }

    
end Controller