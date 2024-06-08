package de.htwg.sa.minesweeper.model.gameComponent

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import de.htwg.sa.minesweeper.model.gameComponent.gameBaseImpl.{Decider, Game, Playfield}
import play.api.libs.json.Json

import scala.concurrent.ExecutionContextExecutor
import scala.util.{Failure, Success}

class ModelApi(using var game: IGame, var field: IField){
    
    implicit val system: ActorSystem = ActorSystem()
    implicit val materializer: Materializer = Materializer(system)
    implicit val executionContext: ExecutionContextExecutor = system.dispatcher

    def createField(leGame: IGame): IField = {
        val adjacentField = Playfield()
        val tempGame: Game = leGame.asInstanceOf[Game]
        adjacentField.newField(leGame.side, tempGame)
    }

    val decider = new Decider()
        
    val route: Route = pathPrefix("model") {
        get {
            path("game") {
                val game = Game(10, 9, 0, "Playing")
                complete(HttpEntity(ContentTypes.`application/json`,game.gameToJson))
            } ~
            path("game"/"helpMessage") {
                val message = game.helpMessage
                complete(HttpEntity(ContentTypes.`application/json`, message))
            } ~
            path("game"/"new") {
                parameter("bombs".as[Int], "size".as[Int], "time".as[Int]) { (bombs, size, time) =>
            
                this.game = Game(bombs, size, time, "Playing") // new Game should always be Playing
                complete(HttpEntity(ContentTypes.`application/json`,game.gameToJson.toString()))

                }
            } ~
            path("game"/"checkExit") {
                parameter("board".as[String]) { (board) =>
                val boolean = game.checkExit(board)
                complete(HttpEntity(ContentTypes.`application/json`, boolean.toString()))
                }
            } ~
            path("field"/"new") {
                parameter("bombs".as[Int], "size".as[Int], "time".as[Int]) { (bombs, size, time) =>
        
                val spiel = Game(bombs, size, time, "Playing")
                this.game = spiel
                val feld = createField(spiel)
                this.field = feld
                complete(HttpEntity(ContentTypes.`application/json`,feld.fieldToJson))
                }
            } ~
            path("field") {
                complete(HttpEntity(ContentTypes.`application/json`,field.fieldToJson.toString()))
            }
        } ~
        put {
            path("field"/"decider") {
                parameter("b".as[Boolean], "x".as[Int], "y".as[Int], "bombs".as[Int], "size".as[Int], "time".as[Int], "board".as[Int]) { (b, x, y, bombs, size, time, board) =>
                    entity(as[String]) { feld =>
                        // doMove
                        val newBoard = board.match
                        {
                            case 0 => "Playing"
                            case 1 => "Won"
                            case 2 => "Lost"
                        }

                        val jasonStringField = feld
                        val controllerField = field.jsonToField(jasonStringField)
                        val controllerGame = Game(bombs, size, time, newBoard)
                        val (tempGame, tempField): (IGame, IField) = decider.evaluateStrategy(b, x, y, controllerField, controllerGame)
                        val jsonGame = Json.parse(tempGame.gameToJson)
                        val jsonField = Json.parse(field.fieldToJson(tempField))
                        val jsonGameFieldArray = Json.arr(jsonGame, jsonField)
                        complete(HttpEntity(ContentTypes.`application/json`, jsonGameFieldArray.toString))
                    }
                }
            } ~
            path("field"/"showInvisibleCell") {
                parameter("y".as[Int], "x".as[Int]) { (y, x) =>
                    entity(as[String]) { feld =>
                        val jasonStringField = feld
                        val fieldFromController = field.jsonToField(jasonStringField)
                        val symbol = fieldFromController.showInvisibleCell(y, x)
                        complete(HttpEntity(ContentTypes.`application/json`, Json.parse(symbol).toString()))
                    }
                }
            } ~
            path("field"/"put"/"flag") {
                parameter("x".as[Int], "y".as[Int]) { (x, y) =>
                    entity(as[String]) { feld =>
                        val jasonStringField = feld
                        val fieldFromController = field.jsonToField(jasonStringField)
                        val flaggedField = fieldFromController.putFlag(x, y)
                        val flaggedJsonField = fieldFromController.fieldToJson(flaggedField)
                        complete(HttpEntity(ContentTypes.`application/json`, Json.parse(flaggedJsonField).toString()))
                    }
                }
            } ~
            path("field"/"put"/"removeFlag") {
                parameter("x".as[Int], "y".as[Int]) { (x, y) =>
                    entity(as[String]) { feld =>
                        val jasonStringField = feld
                        val fieldFromController = field.jsonToField(jasonStringField)
                        val flaggedField = fieldFromController.removeFlag(x, y)
                        val flaggedJsonField = fieldFromController.fieldToJson(flaggedField)
                        complete(HttpEntity(ContentTypes.`application/json`, Json.parse(flaggedJsonField).toString()))
                    }
                }
            } ~
            path("field"/"put") {
                parameter("symbol".as[String], "x".as[Int], "y".as[Int]) { (symbol, x, y) =>
                    entity(as[String]) { feld =>
                        val jasonStringField = feld
                        val fieldFromController = field.jsonToField(jasonStringField)
                        val flaggedField = fieldFromController.put(symbol, x, y)
                        val flaggedJsonField = fieldFromController.fieldToJson(flaggedField)
                        complete(HttpEntity(ContentTypes.`application/json`, Json.parse(flaggedJsonField).toString()))
                    }
                }
            } ~
            path("field"/"recursiveOpen") {
                parameter("x".as[Int], "y".as[Int]) { (x, y) =>
                    entity(as[String]) { feld =>
                        val jasonStringField = feld
                        val saveControllerField = field.jsonToField(jasonStringField)
                        val saveField = saveControllerField.recursiveOpen(x, y, saveControllerField)
                        val openedField = saveField.fieldToJson(saveField)
                        complete(HttpEntity(ContentTypes.`application/json`, Json.parse(openedField).toString()))
                    }
                }
            } ~
            path("field"/"gameOverField") {
                entity(as[String]) { feld =>
                    val jasonStringField = feld
                    // optimization after gatling test
                    val newJsonString = updateMatrixWithHidden(jasonStringField)
                    println("field/gameOverField")
                    complete(HttpEntity(ContentTypes.`application/json`, Json.parse(newJsonString).toString()))
                }
            } ~
            path("field"/"toString") {
                entity(as[String]) { feld =>
                    val jasonStringField = feld
                    val fieldFromController = field.jsonToField(jasonStringField)
                    val saveField = fieldFromController.toString
                    complete(HttpEntity(ContentTypes.`application/json`, saveField))
                }
            } ~
            path("test") {
                entity(as[String]) { entity =>
                    println(entity)
                    complete(HttpEntity(ContentTypes.`application/json`, entity))
                }
            } ~
            path("field"/"cheat") {
                entity(as[String]) { feld =>
                    val jasonStringField = feld
                    val fieldFromController = field.jsonToField(jasonStringField)
                    val saveField = fieldFromController.reveal
                    val prepareJsonField = saveField.toString
                    complete(HttpEntity(ContentTypes.`application/json`, prepareJsonField))
                }
            } ~
            path("game"/"newGameField") {
                parameter("optionString".as[Int]) { (optionString) =>
                    entity(as[String]) { currentGame =>
                        val jasonStringGame = currentGame
                        val saveControllerGame: Game = game.jsonToGame(jasonStringGame).asInstanceOf[Game]
                        val convertedOptionString = optionString match {
                            case 0 => "SuperEasy" 
                            case 1 => "Easy"
                            case 2 => "Medium"
                            case 3 => "Hard"
                            case _ => "Easy"
                        }
                        val controllerOptionString = Some(convertedOptionString)
                        val(feldy, spiely) = saveControllerGame.prepareBoard2(controllerOptionString)(saveControllerGame)

                        val jsonGame = Json.parse(spiely.gameToJson)
                        val jsonField = Json.parse(feldy.fieldToJson(feldy))
                        val jsonGameFieldArray = Json.arr(jsonGame, jsonField)
                        complete(HttpEntity(ContentTypes.`application/json`, jsonGameFieldArray.toString))
                    }
                }
            }
        }
    }

    def updateMatrixWithHidden(jsonString: String): String = {
        // Find the start and end indices of the "matrix" and "hidden" arrays
        val matrixStart = jsonString.indexOf("\"matrix\": [") + "\"matrix\": [".length
        val matrixEnd = jsonString.indexOf("]", matrixStart)
        val hiddenStart = jsonString.indexOf("\"hidden\": [") + "\"hidden\": [".length
        val hiddenEnd = jsonString.indexOf("]", hiddenStart)

        // Extract the matrix and hidden strings
        val matrixString = jsonString.substring(matrixStart, matrixEnd)
        val hiddenString = jsonString.substring(hiddenStart, hiddenEnd)

        // Replace the matrix string with the hidden string
        val newJsonString = jsonString.substring(0, matrixStart) + hiddenString + jsonString.substring(matrixEnd)

        newJsonString
    }

    def start(): Unit = {
        val bindFuture = Http().newServerAt("0.0.0.0", 9082).bind(route)

        bindFuture.onComplete {
            case Success(binding) =>
                println("Server online at http://localhost:9082/")
                complete(binding.toString)
            case Failure(exception) =>
                println(s"An error occurred: $exception")
                complete("fail binding")
        }
    }

}