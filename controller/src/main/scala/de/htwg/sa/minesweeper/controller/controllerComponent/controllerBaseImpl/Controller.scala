package de.htwg.sa.minesweeper.controller.controllerComponent.controllerBaseImpl

import de.htwg.sa.minesweeper.controller.controllerComponent.config.Default
import de.htwg.sa.minesweeper.controller.controllerComponent.IController
import de.htwg.sa.minesweeper.model.gameComponent.gameBaseImpl._
import de.htwg.sa.minesweeper.model.gameComponent._
import de.htwg.sa.minesweeper.persistence.fileIoComponent.IFileIO
import de.htwg.sa.minesweeper.util.{Observable, Move, UndoRedoManager, Event}

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
import de.htwg.sa.minesweeper.util.RestUtil
import play.api.libs.json.JsArray
import play.api.libs.json.Format
import play.api.libs.json.JsResult
import play.api.libs.json.JsSuccess
import play.api.libs.json.JsError

import java.nio.file.{Files, Paths}
import scala.util.{Failure, Success}



class Controller(using var game: IGame, var file: IFileIO) extends IController with Observable:
    
    
    implicit val system: ActorSystem = ActorSystem()
    implicit val materializer: Materializer = Materializer(system)
    implicit val executionContext: ExecutionContextExecutor = system.dispatcher
    

    // val hiddenMatrix = Matrix[String]
    // Matrix[String]
    // Field(Matrix[String], Matrix[String])

    var field: IField = createField(game)
    val undoRedoManager = new UndoRedoManager[IField]

    // approved
    def createField(leGame: IGame): de.htwg.sa.minesweeper.model.gameComponent.IField = {
        val url = s"http://localhost:8082/model/field/new?bombs=${leGame.bombs}&size=${leGame.side}&time=${leGame.time}"

        //TODO: replace field with field controller
        val returnField: de.htwg.sa.minesweeper.model.gameComponent.IField = Try {
            val result = Source.fromURL(url).mkString
            RestUtil.jsonToField(result)
        } match {
            case Success(field) => field
            case Failure(e) =>
                println(s"Error fetching or parsing field data: ${e.getMessage}")
                Field(new Matrix[String](leGame.side, ""), new Matrix[String](leGame.side, "")) // Return a default field or handle the error appropriately
            
        }
        //println(returnField.matrix.toString()) // TODO: remove
        //println(returnField.hidden.toString()) // TODO: remove
        returnField
    }

    // approved
    def doMove(b: Boolean, move: Move, game: IGame) = 
        synchronized{            
            // board parameter should have value 0 if playing and 1 if won and 2 if lost
            val convertedBoard = game.board match {
                case "Playing" => 0
                case "Won" => 1
                case "Lost" => 2
            }
            // ModelAPI call: model/field/decider with parameters b, x, y, bombs size, time, board
            val url = s"http://localhost:8082/model/field/decider?b=${b}&x=${move.x}&y=${move.y}&bombs=${game.bombs}&size=${game.side}&time=${game.time}&board=$convertedBoard"
            val jsonFieldFirst = RestUtil.fieldToJson(field) // provide the controller field to the ModelAPI
            val jsonFileContentFirst = jsonFieldFirst.getBytes("UTF-8")

            val firstRequest = requestPut(url, jsonFileContentFirst) // generate put request with the controller field in body

            val firstResponseFuture: Future[HttpResponse] = Http().singleRequest(firstRequest) // get response
            val firstBodyStringFuture: Future[String] = firstResponseFuture.flatMap { response =>
                response.entity.toStrict(5.seconds).map(_.data.utf8String)
            }

            val returnedJsonGameAndField = Await.result(firstBodyStringFuture, 5.seconds)
            val (newGame, newField): (IGame, IField) = RestUtil.jsonToGameAndField(returnedJsonGameAndField) // JsonArray with Game and Field converting
            field = newField
            this.game = newGame
            
            val jasonField2 = RestUtil.fieldToJson(field)
            val jsonFileContent2 = jasonField2.getBytes("UTF-8")

            val request2 = HttpRequest(
                method =  HttpMethods.PUT,
                uri = s"http://localhost:8082/model/field/showInvisibleCell?y=${move.y}&x=${move.x}",
                entity = HttpEntity(ContentTypes.`application/json`, jsonFileContent2)
            )

            val responseFuture: Future[HttpResponse] = Http().singleRequest(request2)
            val bodyStringFuture: Future[String] = responseFuture.flatMap { response =>
                response.entity.toStrict(5.seconds).map(_.data.utf8String)
            }

            val result = Await.result(bodyStringFuture, 5.seconds)
            val symbol = if (result.length()>3) {"E"} else {result}
            //println("\u001B[33m" + symbol + "_doMove_" + "\u001B[0m")

            if(symbol == "E"){field}
            else if(symbol == "0"){field}
            else{undoRedoManager.doStep(field, DoCommand(move))}
        }
    
    // approved
    def loadGame = {

        // _____________________________________________________________________________________________________
        // url
        val uri = Uri("http://localhost:8083/persistence/game") // load the game

        val request = HttpRequest(method = HttpMethods.GET, uri = uri)
        val responseFuture: Future[HttpResponse] = Http().singleRequest(request)
        val bodyStringFuture: Future[String] = responseFuture.flatMap { response =>
            response.entity.toStrict(5.seconds).map(_.data.utf8String)
        }
        val result = Await.result(bodyStringFuture, 5.seconds)

        // now we need to parse the resulting jsong string representig the Game
        val loadedGame: Option[IGame] = Try {
            RestUtil.jsonToGame(result)
        } match {
            case Success(game) => println("game loaded"); Some(game)
            case Failure(e) => None
        }

        val gameOption = loadedGame

        //val gameOption = file.loadGame
        gameOption match {
            case Some(game) => this.game = game
            case None =>
        }
        // _____________________________________________________________________________________________________
        game = Game(gameOption.get.bombs, gameOption.get.side, gameOption.get.time, gameOption.get.board)
        // _____________________________________________________________________________________________________
        // we need to load the field too
        val uriField = Uri("http://localhost:8083/persistence/field") // load the field

        val requestField = HttpRequest(method = HttpMethods.GET, uri = uriField)
        val responseFutureField: Future[HttpResponse] = Http().singleRequest(requestField)
        val bodyStringFutureField: Future[String] = responseFutureField.flatMap { response =>
            response.entity.toStrict(5.seconds).map(_.data.utf8String)
        }
        val resultField = Await.result(bodyStringFutureField, 5.seconds)
        val loadedField: Option[IField] = Try {
            RestUtil.jsonToField(resultField)
        } match {
            case Success(field) => println("field loaded"); Some(field)
            case Failure(e) => None
        }

        val fieldOption = loadedField

        //val fieldOption = file.loadField
        fieldOption match {
            case Some(field) => this.field = field
            case None =>
        }

        //notifyObservers(Event.Load)
        notifyObserversRest("Load")
        // _____________________________________________________________________________________________________
    }


    
    // approved
    def saveGame =

        //notifyObservers(Event.SaveTime)
        notifyObserversRest("SaveTime")

        // _____________________________________________________________________________________________________
        val queryParameters = Uri.Query(
            "bombs" -> s"${game.bombs}",
            "size"  -> s"${game.side}",
            "time"  -> s"${game.time}",
            "board" -> s"${game.board}"
        )

        val uri = Uri("http://localhost:8083/persistence/putGame").withQuery(queryParameters)
        val request = HttpRequest(method = HttpMethods.PUT, uri = uri)

        val responseFuture: Future[HttpResponse] = Http().singleRequest(request)

        responseFuture.onComplete {
            case Success(res) => println(s"Request successful: $res")
            case Failure(ex)  => println(s"Request failed: $ex")
        }

        //file.saveGame(game)
        // _____________________________________________________________________________________________________

        // _____________________________________________________________________________________________________

        val jasonField = field.fieldToJson
        val jsonFileContent = jasonField.getBytes("UTF-8")

        val request2 = HttpRequest(
            method =  HttpMethods.PUT,
            uri = "http://localhost:8083/persistence/putField",
            entity = HttpEntity(ContentTypes.`application/json`, jsonFileContent)
        )

        val responseFuture2: Future[HttpResponse] = Http().singleRequest(request2)

        responseFuture2.onComplete {
            case Success(res) => println(s"Successfully saved JSON file field: ${res.entity}")
            case Failure(ex)  => println(s"Failed to put JSON file: ${ex.getMessage}")
        }

        //file.saveField(field)
        // _____________________________________________________________________________________________________

        game = Game(game.bombs, game.side, game.time, "Playing")
        //notifyObservers(Event.Save)
        notifyObserversRest("Save")
    
    def exit = notifyObserversRest("Exit")//notifyObservers(Event.Exit) // approved

    // approved
    def gameOver =
        val url = s"http://localhost:8082/model/field/gameOverField"
        val jsonField = RestUtil.fieldToJson(field) // provide the controller field to the ModelAPI
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
        field = RestUtil.jsonToField(bodyString)

        //notifyObservers(Event.GameOver)
        notifyObserversRest("GameOver")
    
    def openRec(x: Int, y: Int, field: IField): IField = undoRedoManager.doStep(field, DoCommand(Move("recursion", x, y))) // approved

    // approved
    def helpMenu = 
        helpMenuRest
/*         val url = s"http://localhost:8082/model/game/helpMessage"
        // prepare the GET request
        val request = HttpRequest(
            method =  HttpMethods.GET,
            uri = url
        )
        // now get the message from the modelApi
        val responseFuture: Future[HttpResponse] = Http().singleRequest(request)
        val bodyStringFuture: Future[String] = responseFuture.flatMap { response =>
            response.entity.toStrict(5.seconds).map(_.data.utf8String)
        }
        val bodyStringHelpMessage = Await.result(bodyStringFuture, 5.seconds)
        // println(bodyString) maybe later !!!
        println("this is helpMenue from Controller")
 */
        //game.helpMessage 


        
        //notifyObservers(Event.Help)
        notifyObserversRest("Help")
    
    // approved
    def fieldToString: String = {
        val url2 = s"http://localhost:8082/model/field/toString"
        // prepare the Field
        val bodyField = RestUtil.fieldToJson(field)
        // prepare the PUT request
        val request2 = HttpRequest(
            method =  HttpMethods.PUT,
            uri = url2,
            entity = HttpEntity(ContentTypes.`application/json`, bodyField)
        )
        // now get the printed field from the modelApi
        val responseFuture2: Future[HttpResponse] = Http().singleRequest(request2)
        val bodyStringFuture2: Future[String] = responseFuture2.flatMap { response =>
            response.entity.toStrict(5.seconds).map(_.data.utf8String)
        }
        val bodyString2 = Await.result(bodyStringFuture2, 5.seconds)
        
        //println(field.toString)
        bodyString2

    }

    // approved
    def helpMenuRest: String = 
        val url = s"http://localhost:8082/model/game/helpMessage"
        // prepare the GET request
        val request = HttpRequest(
            method =  HttpMethods.GET,
            uri = url
        )
        // now get the message from the modelApi
        val responseFuture: Future[HttpResponse] = Http().singleRequest(request)
        val bodyStringFuture: Future[String] = responseFuture.flatMap { response =>
            response.entity.toStrict(5.seconds).map(_.data.utf8String)
        }
        val bodyStringHelpMessage = Await.result(bodyStringFuture, 5.seconds)
        // println(bodyString) maybe later !!!

/*         println("this is helpMenue from Controller")

        //game.helpMessage

        val url2 = s"http://localhost:8082/model/field/toString"
        // prepare the Field
        val bodyField = RestUtil.fieldToJson(field)
        // prepare the PUT request
        val request2 = HttpRequest(
            method =  HttpMethods.PUT,
            uri = url2,
            entity = HttpEntity(ContentTypes.`application/json`, bodyField)
        )
        // now get the printed field from the modelApi
        val responseFuture2: Future[HttpResponse] = Http().singleRequest(request2)
        val bodyStringFuture2: Future[String] = responseFuture2.flatMap { response =>
            response.entity.toStrict(5.seconds).map(_.data.utf8String)
        }
        val bodyString2 = Await.result(bodyStringFuture2, 5.seconds)
        println(bodyString2) // !!!  */
        //println(field.toString)
        //notifyObservers(Event.Help) <--
        bodyStringHelpMessage
    
    //approved
    def cheat = 
        cheatRest
        notifyObserversRest("Cheat")
    
    def cheatRest: String = {
        val url = s"http://localhost:8082/model/field/cheat"
        // prepare the Field
        val bodyField = RestUtil.fieldToJson(field)
        // prepare the PUT request
        val request = HttpRequest(
            method =  HttpMethods.PUT,
            uri = url,
            entity = HttpEntity(ContentTypes.`application/json`, bodyField)
        )
        // now get the printed field from the modelApi
        val responseFuture: Future[HttpResponse] = Http().singleRequest(request)
        val bodyStringFuture: Future[String] = responseFuture.flatMap { response =>
            response.entity.toStrict(5.seconds).map(_.data.utf8String)
        }
        val bodyString = Await.result(bodyStringFuture, 5.seconds)
        //println(bodyString) maybe later !!!
        //field.reveal
        bodyString
        
    }

    // approved
    def checkGameOver(status: String) = {
        //game.checkExit(status)
        val url = s"http://localhost:8082/model/game/checkExit?board=$status"
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
        
    // approved
    def newGameGUI =
        game = Game(game.bombs, game.side, game.time, "Playing")
        //notifyObservers(Event.Input)
        notifyObserversRest("Input")
    
    // approved
    def newGameField(optionString: Option[String]) =
        game = Game(game.bombs, game.side, game.time, "Playing")
        //val (feld, spiel): (IField, IGame) = game.prepareBoard(optionString)(game)

        val oString = optionString.get match {
            case "SuperEasy" => 0
            case "Easy" => 1 
            case "Medium" => 2
            case "Hard" => 3
        }
        
        val url = s"http://localhost:8082/model/game/newGameField?optionString=$oString" // we need a url
        //println(s"newGameField: url = $url")

        val bodyGame = RestUtil.gameToJson(game) // prepare the game
        val body = bodyGame.getBytes("UTF-8")

        val request = requestPut(url, body)  // prepare the PUT request

        val responseFuture: Future[HttpResponse] = Http().singleRequest(request)
        val bodyStringFuture: Future[String] = responseFuture.flatMap { response =>
            response.entity.toStrict(5.seconds).map(_.data.utf8String)
        }
        val returnedJsonGameAndField = Await.result(bodyStringFuture, 5.seconds)
        val (newGame, newField): (IGame, IField) = RestUtil.jsonToGameAndField(returnedJsonGameAndField)
        field = newField                
        game = Game(newGame.bombs, newGame.side, newGame.time, newGame.board) 

        //field = feld
        //game = spiel
        
        //notifyObservers(Event.NewGame)
        notifyObserversRest("NewGame")
        
    // approved
    def newGame(side: Int, bombs: Int) =
        game = Game(bombs, side, game.time, "Playing")
        field = createField(game) // TODO: replace with GameEntity
        //notifyObservers(Event.NewGame)
        notifyObserversRest("NewGame")
    
    // approved - doMove from TUI transfered as param -> game & field in TUI declared
    def makeAndPublish(makeThis: (Boolean, Move, IGame) => IField, b: Boolean, move: Move, game: IGame): Unit =
        // "open" => controller.makeAndPublish(controller.doMove, firstMoveCheck, move, controller.game)
        field = makeThis(b, move, game)  // doMove
        
        val jasonField = RestUtil.fieldToJson(field)
        val jsonFileContent = jasonField.getBytes("UTF-8")
        val request = requestPut(s"http://localhost:8082/model/field/showInvisibleCell?y=${move.y}&x=${move.x}", jsonFileContent) // -> request to ModelAPi
        val responseFuture: Future[HttpResponse] = Http().singleRequest(request)
        val bodyStringFuture: Future[String] = responseFuture.flatMap { response =>
            response.entity.toStrict(5.seconds).map(_.data.utf8String)
        }

        val result = Await.result(bodyStringFuture, 5.seconds)
        val symbol = if(result.length > 3){"E"} else {result}

        if (symbol == "0"){field = openRec(move.x,move.y,field)}
/*         val firstOrNext = if (b) Event.Start else Event.Next
        notifyObservers(firstOrNext) */
        val firstOrNext = if (b) "Start" else "Next"
        notifyObserversRest(firstOrNext)
    

    // approved
    def makeAndPublish(makeThis: Move => IField, move: Move): Unit =
        move.value match {
            case "flag" => 
                val jasonField = RestUtil.fieldToJson(field)
                val jsonFileContent = jasonField.getBytes("UTF-8")

                val request = HttpRequest(
                    method =  HttpMethods.PUT,
                    uri = s"http://localhost:8082/model/field/put/flag?x=${move.x}&y=${move.y}",
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

                val fieldFromController = RestUtil.jsonToField(jsonBodyField)
                field = fieldFromController

            case "unflag" => 
                val jasonField = RestUtil.fieldToJson(field)
                val jsonFileContent = jasonField.getBytes("UTF-8")

                val request2 = HttpRequest(
                    method =  HttpMethods.PUT,
                    uri = s"http://localhost:8082/model/field/put/removeFlag?x=${move.x}&y=${move.y}",
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

                val fieldFromController = RestUtil.jsonToField(jsonBodyField)
                field = fieldFromController
        }

        field = makeThis(move)
        //notifyObservers(Event.Next)
        notifyObserversRest("Next")

    // approved
/*     def makeAndPublish(makeThis: => IField) =
        field = makeThis
        //notifyObservers(Event.Next)
        notifyObserversRest("Next") */

    def makeAndPublish(makeThis: => IField): IField =
        field = makeThis
        //notifyObservers(Event.Next)
        notifyObserversRest("Next")
        field

    
    
    // approved
    def saveScoreAndPlayerName(playerName: String, saveScore: Int, filePath: String) = {
        //file.savePlayerScore(playerName, saveScore, filePath)
        val queryParameters = Uri.Query(
            "player" -> s"${playerName}",
            "score"  -> s"${saveScore}"
        )

        val uri = Uri("http://localhost:8083/persistence/putHighscore").withQuery(queryParameters)
        val request = HttpRequest(method = HttpMethods.PUT, uri = uri)

        val responseFuture: Future[HttpResponse] = Http().singleRequest(request)

        responseFuture.onComplete {
        case Success(res) => println(s"Request successful: $res")
        case Failure(ex)  => println(s"Request failed: $ex")
        }
    }

    // approved
    def loadPlayerScores = {
        
        val url = "http://localhost:8083/persistence/highscore"
        val request = HttpRequest(method = HttpMethods.GET, uri = url)
        val responseFuture: Future[HttpResponse] = Http().singleRequest(request)
        val bodyFuture: Future[String] = responseFuture.flatMap { response =>
            response.entity.toStrict(5.seconds).map(_.data.utf8String)
        }
        val jsonStringApiResult: String = Await.result(bodyFuture, 5.seconds)
        
        // body needs to be convertoed to a Seq[(String, Int)]
        case class PlayerScore(player: String, score: Int)
        implicit val playerScoreFormat: Format[PlayerScore] = Json.format[PlayerScore]
        
        val json: JsValue = Json.parse(jsonStringApiResult) // Parse the JSON string into a sequence of PlayerScore objects
        val playerScoresResult: JsResult[Seq[PlayerScore]] = Json.fromJson[Seq[PlayerScore]](json)

        // Map the sequence of PlayerScore objects to the desired Seq[(String, Int)]
        val playerScores: Seq[(String, Int)] = playerScoresResult match {
        case JsSuccess(scores, _) => scores.map(ps => (ps.player, ps.score))
        case JsError(errors) => Seq.empty // Handle the error appropriately
        }

        //file.loadPlayerScores
        playerScores
    }
    
    def put(move: Move): IField = undoRedoManager.doStep(field, DoCommand(move)) // approved
    def undo: IField = undoRedoManager.undoStep(field) // approved
    def redo: IField = undoRedoManager.redoStep(field) // approved

    // approved
    def saveTime(currentTime: Int): Unit = 
        val tempGame: IGame = Game(game.bombs, game.side, currentTime, "Playing") // TODO: use Entity Game
        game = tempGame
    
    // approved
    def requestPut(input_uri: String, jsonContent: Array[Byte]) = {
        HttpRequest(
            method =  HttpMethods.PUT,
            uri = input_uri,
            entity = HttpEntity(ContentTypes.`application/json`, jsonContent)
        )
    }

    // approved - but only to notify the TUI
    def notifyObserversRest(event: String) = {
        // TODO: maybe register the logic

        // _________________________ NOTIFY TUI _________________________

        val uri = s"http://localhost:8088/tui/notify" + "?event=" + event

        val request = HttpRequest(
            method =  HttpMethods.PUT,
            uri = uri
        )
        
        val responseFuture: Future[HttpResponse] = Http().singleRequest(request)
        val bodyFieldFuture: Future[String] = responseFuture.flatMap { response =>
            response.entity.toStrict(5.seconds).map(_.data.utf8String)
        }
/*         val result = Await.result(bodyFieldFuture, 5.seconds)
        println(result) */

        // _________________________ NOTIFY GUI _________________________
        val uri2 = s"http://localhost:8087/gui/notify" + "?event=" + event

        val request2 = HttpRequest(
            method =  HttpMethods.PUT,
            uri = uri2
        )
        
        val responseFuture2: Future[HttpResponse] = Http().singleRequest(request2)
        val bodyFieldFuture2: Future[String] = responseFuture2.flatMap { response =>
            response.entity.toStrict(5.seconds).map(_.data.utf8String)
        }
/*         val result2 = Await.result(bodyFieldFuture2, 5.seconds)
        println(result2) */


        
    }

    
end Controller