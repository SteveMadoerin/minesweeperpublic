package de.htwg.sa.minesweeper.model.gameComponent
import de.htwg.sa.minesweeper.model.gameComponent.{IGame, IField}

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, Materializer}
import scala.concurrent.ExecutionContext
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.RouteDirectives
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Route, StandardRoute}
import scala.concurrent.ExecutionContextExecutor
import scala.util.Failure
import scala.util.Success
import scala.util.matching.Regex
import scala.util.Try
import de.htwg.sa.minesweeper.model.gameComponent.gameBaseImpl.Game
import de.htwg.sa.minesweeper.model.gameComponent.gameBaseImpl.Playfield
import de.htwg.sa.minesweeper.model.gameComponent.config.Default
import de.htwg.sa.minesweeper.model.gameComponent.gameBaseImpl.Decider
import scala.compiletime.ops.boolean
import play.api.libs.json.Json
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.concurrent.Future

class ModelApi(using var game: IGame, var field: IField){
    
    implicit val system: ActorSystem = ActorSystem()
    implicit val materializer: Materializer = Materializer(system)
    implicit val executionContext: ExecutionContextExecutor = system.dispatcher



    def createField(leGame: IGame): IField = {
        val adjacentField = Playfield()
        val tempGame: Game = leGame.asInstanceOf[Game]
        adjacentField.newField(leGame.side, tempGame)
    }

    val decider = new Decider() // is needed to decide if it is firstmove or not
        
    val route: Route = pathPrefix("model") {
        get {
            path("game") {
                // TODO:   
                val game = new Game(10, 9, 0, "Playing")
                complete(HttpEntity(ContentTypes.`application/json`,game.gameToJson))
            } ~
            path("game"/"helpMessage") {
                val message = game.helpMessage
                complete(HttpEntity(ContentTypes.`application/json`, message))
            } ~
            path("game"/"new") {
                // TODO: Controller when creating new game should call here (1.)
                parameter("bombs".as[Int], "size".as[Int], "time".as[Int]) { (bombs, size, time) =>
            
                this.game = Game(bombs, size, time, "Playing") // new Game should always be Playing
                complete(HttpEntity(ContentTypes.`application/json`,game.gameToJson.toString()))

                }
            } ~
            path("game"/"checkExit") {
                parameter("board".as[String]) { (board) =>
                val boolean = game.checkExit(board)
                complete(HttpEntity(ContentTypes.`application/json`, boolean.toString())) // boolean to json
                }
            } ~
            path("field"/"new") {
                // TODO: Controller when creating new field should call here (1.)
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
                        val controllerField = field.jsonToField(jasonStringField) // TODO: check jasonToField
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
                        val fieldFromController = field.jsonToField(jasonStringField) // TODO: check jasonToField
                        val symbol = fieldFromController.showInvisibleCell(y, x)
                        // Use both jsonField and symbol as needed
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
                    val fieldFromController = field.jsonToField(jasonStringField) // TODO: check jasonToField
                    val saveField = fieldFromController.gameOverField
                    val prepareJsonField = fieldFromController.fieldToJson(saveField)
                    complete(HttpEntity(ContentTypes.`application/json`, Json.parse(prepareJsonField).toString()))
                }
            } ~
            path("field"/"toString") {
                entity(as[String]) { feld =>
                    val jasonStringField = feld
                    val fieldFromController = field.jsonToField(jasonStringField) // TODO: check jasonToField
                    val saveField = fieldFromController.toString
                    complete(HttpEntity(ContentTypes.`application/json`, saveField))
                }
            } ~
            path("field"/"cheat") {
                entity(as[String]) { feld =>
                    val jasonStringField = feld
                    val fieldFromController = field.jsonToField(jasonStringField) // TODO: check jasonToField
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
/*             path("putField") {
                entity(as[String]) { field =>
                    val jsonField = field
                    val symbol = "1"

                    complete(HttpEntity(ContentTypes.`application/json`, Json.parse(symbol).toString))

                }
            } */ /* ~
            path("putGame") {
                parameter("bombs".as[Int], "size".as[Int], "time".as[Int], "board".as[String]) { (bombs, size, time, board) =>
            
                    val testGame: IGame = Game(bombs, size, time, board)
                    file.saveGame(testGame)
                    complete(HttpEntity(ContentTypes.`application/json`, Game(bombs, size, time, board).gameToJson.toString()))
                }
            } */  /* ~
            path("putHighscore") {
                parameter("player".as[String], "score".as[Int]) { (player, score) =>

                    val newScoreObj = Json.obj(
                        "player" -> player,
                        "score" -> score
                    )

                    val pathToFile = "C:\\Playground\\minesweeperpublic\\src\\main\\data\\highscore.json"

                    file.savePlayerScore(player, score, pathToFile)
                    complete(HttpEntity(ContentTypes.`application/json`, newScoreObj.toString())) 

                }
            } */
        }
    }

    val bindFuture = Http().bindAndHandle(route, "localhost", 8082)

    def unbind = bindFuture
        .flatMap(_.unbind())
        .onComplete(_ => system.terminate())

    def start: Future[Nothing] = Await.result(Future.never, Duration.Inf)
  

}