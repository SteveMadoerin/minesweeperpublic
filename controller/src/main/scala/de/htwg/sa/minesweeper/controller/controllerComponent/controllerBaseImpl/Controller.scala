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



class Controller(using var game: IGame, var file: IFileIO) extends IController with Observable:
    
    
    implicit val system: ActorSystem = ActorSystem()
    implicit val materializer: Materializer = Materializer(system)
    implicit val executionContext: ExecutionContextExecutor = system.dispatcher
    

    // val hiddenMatrix = Matrix[Symbol]
    // Matrix[Symbol]
    // Field(Matrix[Symbol], Matrix[Symbol])

    var field: IField = createField(game)
    //val decider = new Decider()
    val undoRedoManager = new UndoRedoManager[IField]


    // def createField is working
    def createField(leGame: IGame): de.htwg.sa.minesweeper.model.gameComponent.IField = {
        val url = s"http://localhost:8082/model/field/new?bombs=${leGame.bombs}&size=${leGame.side}&time=${leGame.time}"

        //TODO: replace field with field controller
        val returnField: de.htwg.sa.minesweeper.model.gameComponent.IField = Try {
            val result = Source.fromURL(url).mkString
            RestUtil.jsonToField(result) //field.jsonToField(result)
        } match {
            case Success(field) => field
            case Failure(e) =>
                println(s"Error fetching or parsing field data: ${e.getMessage}")
                Field(new Matrix[String](leGame.side, ""), new Matrix[String](leGame.side, "")) // Return a default field or handle the error appropriately
            
        }
        println(returnField.matrix.toString()) // TODO: remove
        println(returnField.hidden.toString()) // TODO: remove
        
        returnField
    }

    // doMove is working
    def doMove(b: Boolean, move: Move, game: IGame) = 
        synchronized{
            // attention//val (tempGame, tempField): (IGame, IField) = decider.evaluateStrategy(b, move.x, move.y, field, game)
            
            // board parameter should have value 0 if playing and 1 if won and 2 if lost
            val convertedBoard = game.board match {
                case "Playing" => 0
                case "Won" => 1
                case "Lost" => 2
            }
            // ModelAPI call: model/field/decider with parameters b, x, y, bombs size, time, board
            val url = s"http://localhost:8082/model/field/decider?b=${b}&x=${move.x}&y=${move.y}&bombs=${game.bombs}&size=${game.side}&time=${game.time}&board=$convertedBoard"
            // provide the controller field to the ModelAPI
            val jsonFieldFirst = RestUtil.fieldToJson(field)
            val jsonFileContentFirst = jsonFieldFirst.getBytes("UTF-8")
            // generate put request with the controller field in body
            val firstRequest = requestPut(url, jsonFileContentFirst)
            // get response
            val firstResponseFuture: Future[HttpResponse] = Http().singleRequest(firstRequest)
            val firstBodyStringFuture: Future[String] = firstResponseFuture.flatMap { response =>
                response.entity.toStrict(5.seconds).map(_.data.utf8String)
            }

            // ____________________________ TESTING ____________________________

            val returnedJsonGameAndField = Await.result(firstBodyStringFuture, 5.seconds)
            // then get the packed Game and Field in a JsonArray  [Field, Game] returnedJsonGameAndField and unpack it here
            val (newGame, newField): (IGame, IField) = RestUtil.jsonToGameAndField(returnedJsonGameAndField)
            //println("compared -  game : " + game.board + " newGame: " + newGame.board)
            //println("compared -  game : " + game.bombs + " newGame: " + newGame.bombs)
            field = newField // attention//
            this.game = newGame // attention//
            
            // ____________________________ TESTING ____________________________
            
            // attention//field = tempField 
            // attention//this.game = tempGame
            
            val jasonField3 = RestUtil.fieldToJson(field)//val jasonField = field.fieldToJson
            val jsonFileContent = jasonField3.getBytes("UTF-8")

            val request2 = HttpRequest(
                method =  HttpMethods.PUT,
                uri = s"http://localhost:8082/model/field/showInvisibleCell?y=${move.y}&x=${move.x}",
                entity = HttpEntity(ContentTypes.`application/json`, jsonFileContent)
            )

            val responseFuture: Future[HttpResponse] = Http().singleRequest(request2)
            val bodyStringFuture: Future[String] = responseFuture.flatMap { response =>
                response.entity.toStrict(5.seconds).map(_.data.utf8String)
            }

/*             var symbol = "E"
            bodyStringFuture.onComplete {
                case Success(bodyString) =>
                    symbol = bodyString
                    println("\u001B[33m" + symbol + "_doMove_" + "\u001B[0m")
                case Failure(ex) =>
                    sys.error(s"something wrong: ${ex.getMessage}")
            } */

            //symbol = Await.result(bodyStringFuture, 5.seconds)
            val result = Await.result(bodyStringFuture, 5.seconds)
            val symbol = if (result.length()>3) {"E"} else {result}
            //println("\u001B[33m" + symbol + "_doMove_" + "\u001B[0m")

            if(symbol == "E"){/* println("Error: Symbol not fetched from server"); */field}
            else if(symbol == "0"){field}
            else{undoRedoManager.doStep(field, DoCommand(move))} // TODO doStep 
        }
    
    // TODO: implement
    def loadGame =

        val gameOption = file.loadGame
        gameOption match {
            case Some(game) => this.game = game
            case None =>
        }

        game = Game(gameOption.get.bombs, gameOption.get.side, gameOption.get.time, gameOption.get.board)

        val fieldOption = file.loadField
        fieldOption match {
            case Some(field) => this.field = field
            case None =>
        }

        notifyObservers(Event.Load)
    
    def saveGame =

        notifyObservers(Event.SaveTime)
        file.saveGame(game)
        file.saveField(field)

        game = Game(game.bombs, game.side, game.time, "Playing")
        notifyObservers(Event.Save)
    
    def exit = notifyObservers(Event.Exit)

    // is working now and rest conform
    def gameOver =
        //field = field.gameOverField
        val url = s"http://localhost:8082/model/field/gameOverField"
        // provide the controller field to the ModelAPI
        val jsonField = RestUtil.fieldToJson(field)
        val jsonFileContent = jsonField.getBytes("UTF-8")
        // make a getRequest
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
        val gameOverField = RestUtil.jsonToField(bodyString)
        //println(gameOverField.toString())
        field = gameOverField

        notifyObservers(Event.GameOver)
    
    // is working via Rest
    def openRec(x: Int, y: Int, field: IField): IField = undoRedoManager.doStep(field, DoCommand(Move("recursion", x, y))) // is approved

    // TODO: implement
    def helpMenu = 
        game.helpMessage
        println(field.toString)
        notifyObservers(Event.Help)
/*     def processMove(move: Move, firstMoveCheck: Boolean): Boolean = {
        move.value match {
            case "open" => controller.makeAndPublish(controller.doMove, firstMoveCheck, move, controller.game); true
            case "flag" => controller.makeAndPublish(controller.put, move); true
            case "unflag" => controller.makeAndPublish(controller.put, move); true
            case "help" => controller.helpMenu; true
            case _ => infoMessages(">> Invalid Input"); false
        }
    } */
    // TODO: implement
    def cheat = 
        field.reveal
        notifyObservers(Event.Cheat)
    // TODO: implement
    def checkGameOver(status: String) = game.checkExit(status)

    def newGameGUI =
        game = Game(game.bombs, game.side, game.time, "Playing")
        notifyObservers(Event.Input)
    
    //for GUI
    def newGameField(optionString: Option[String]) =
        game = Game(game.bombs, game.side, game.time, "Playing")
        val prepareWithDifficulty = game.prepareBoard(optionString)_ // partially applied and get a function
        val (feld, spiel): (IField, IGame) = prepareWithDifficulty(game)// complete preparation with game instance
        field = feld
        game = spiel
        
        notifyObservers(Event.NewGame)
        
    def newGame(side: Int, bombs: Int) =
        game = Game(bombs, side, game.time, "Playing")
        field = createField(game)
        notifyObservers(Event.NewGame)
    // Is jetzt rest conform
    // doMove wird von TUI als parameter übergeben hierzu wird game und field in der TUI Klasse angegeben
    def makeAndPublish(makeThis: (Boolean, Move, IGame) => IField, b: Boolean, move: Move, game: IGame): Unit =
        
        // "open" => controller.makeAndPublish(controller.doMove, firstMoveCheck, move, controller.game)
        field = makeThis(b, move, game)  // doMove
        
        //// -> request to ModelAPi
        import system.dispatcher // to get an execution context
        val jasonField = field.fieldToJson(field) // TODO: fieldToJson nach RestUtil
        val jsonFileContent = jasonField.getBytes("UTF-8")
        val request2 = requestPut(s"http://localhost:8082/model/field/showInvisibleCell?y=${move.y}&x=${move.x}", jsonFileContent)
        val responseFuture: Future[HttpResponse] = Http().singleRequest(request2)
        val bodyStringFuture: Future[String] = responseFuture.flatMap { response =>
            response.entity.toStrict(5.seconds).map(_.data.utf8String)
        }

/*         var symbol = "E"
        bodyStringFuture.onComplete {
            case Success(bodyString) =>
                symbol = bodyString
                val ANSI_BLUE = "\u001B[34m"
                val ANSI_RESET = "\u001B[0m"

                println(ANSI_BLUE + symbol + ANSI_RESET)
            case Failure(ex) =>
                sys.error(s"something wrong: ${ex.getMessage}")
        } */

        //TOTOOTOTOTOTOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO
/*         val result = Await.result(bodyStringFuture, 5.seconds)
        symbol = result */
        val result = Await.result(bodyStringFuture, 5.seconds)
        val symbol = if(result.length > 3){"E"} else {result}

/*         val ANSI_BLUE = "\u001B[34m"
        val ANSI_RESET = "\u001B[0m"

        println(ANSI_BLUE + symbol + ANSI_RESET) */

        //if (field.showInvisibleCell(move.y, move.x) == "0"){field = openRec(move.x,move.y,field)}
        if (symbol == "0"){field = openRec(move.x,move.y,field)}
        val firstOrNext = if (b) Event.Start else Event.Next
        notifyObservers(firstOrNext)
    

    // is rest conform now and working
    def makeAndPublish(makeThis: Move => IField, move: Move): Unit =
        //controller.put, move
        move.value match {
            case "flag" => 
                //field = field.putFlag(move.x, move.y)
                import system.dispatcher // to get an execution context

                val jasonField = field.fieldToJson(field) // TODO: replace fieldToJson with own function for that in Controller
                val jsonFileContent = jasonField.getBytes("UTF-8")

                val request2 = HttpRequest(
                    method =  HttpMethods.PUT,
                    uri = s"http://localhost:8082/model/field/put/flag?x=${move.x}&y=${move.y}",
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
                        val ANSI_BLUE = "\u001B[34m"
                        val ANSI_RESET = "\u001B[0m"

                        println(ANSI_BLUE + jsonBodyField + ANSI_RESET)
                    case Failure(ex) =>
                        sys.error(s"something wrong: ${ex.getMessage}")
                }

                val result = Await.result(bodyFieldFuture, 5.seconds)
                jsonBodyField = result

                val fieldFromController = field.jsonToField(jsonBodyField)
                field = fieldFromController

            case "unflag" => 
                //field = field.removeFlag(move.x, move.y)

                import system.dispatcher // to get an execution context

                val jasonField = field.fieldToJson(field) // TODO: replace fieldToJson with own function for that in Controller
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

                val fieldFromController = field.jsonToField(jsonBodyField)
                field = fieldFromController
        }

        //field = makeThis(move)
        notifyObservers(Event.Next)

    // should be restconform and working
    def makeAndPublish(makeThis: => IField) =
        field = makeThis
        notifyObservers(Event.Next)
    
    // TODO implement
    def saveScoreAndPlayerName(playerName: String, saveScore: Int, filePath: String) = {
        file.savePlayerScore(playerName, saveScore, filePath)
    }

    // TODO implement
    def loadPlayerScores(filePath: String) = {
        file.loadPlayerScores(filePath)
    }
    
    def put(move: Move): IField = undoRedoManager.doStep(field, DoCommand(move)) // Stays in Controller
    def undo: IField = undoRedoManager.undoStep(field) // Stays in Controller
    def redo: IField = undoRedoManager.redoStep(field) // Stays in Controller

    // works now
    def saveTime(currentTime: Int): Unit = 
        val tempGame: IGame = Game(game.bombs, game.side, currentTime, "Playing") // TODO: use Entity Game
        game = tempGame
        //game = tempGame.asInstanceOf[IGame] // TODO: delete it later

    def requestPut(input_uri: String, jsonContent: Array[Byte]) = {
        HttpRequest(
            method =  HttpMethods.PUT,
            uri = input_uri,
            entity = HttpEntity(ContentTypes.`application/json`, jsonContent)
        )
    }

    
end Controller