/*package de.htwg.sa.minesweeper.ui

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, Materializer}

import concurrent.duration.DurationInt
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.*

import scala.concurrent.{Await, ExecutionContext, ExecutionContextExecutor, Future}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.RouteDirectives
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.server.{Route, StandardRoute}
import de.htwg.sa.minesweeper.ui.config.Default.given

import scala.util.Failure
import scala.util.Success
import scala.util.matching.Regex
import scala.util.Try
import de.htwg.sa.minesweeper.ui.gui.RestUtil
import de.htwg.sa.minesweeper.ui.model.{Event, FieldTui, GameTui, MatrixTui, Move}


class WebGuiApi() {

    var controllerGame: GameTui = GameTui(9, 10, 0, "Playing")
    var controllerField: FieldTui = RestUtil.requestControllerField
    //controller.add(this)

    implicit val system: ActorSystem = ActorSystem("WebGuiApi")
    implicit val materializer: Materializer = Materializer(system)
    implicit val executionContext: ExecutionContextExecutor = system.dispatcher

    val route: Route = {
        get {
            path("ui") {
                complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>THE MINESWEEPER GAME</h1>"))
            } ~ 
            path("ui"/"new"/"small") {
                val (feldy, gamey) = RestUtil.requestNewGame(9, 10)
                controllerGame = gamey
                controllerField = feldy

                fieldToHtmlNew
            } ~ 
            path("ui"/"undo") {

                //controller.makeAndPublish(controller.undo)
                RestUtil.requestControllerMakeAndPublishUndo
                RestUtil.requestControllerField
                fieldToHtmlNew
            } ~ 
            path("ui"/"redo") {
                //controller.makeAndPublish(controller.redo)
                RestUtil.requestControllerMakeAndPublishRedo
                RestUtil.requestControllerField
                fieldToHtmlNew
                //fieldToHTML(controller.field.toString)
            } ~ 
            path("minesweeper" / Segment) {
                command => {
                    controllerGame = RestUtil.requestControllerGame
                    controllerField = RestUtil.requestControllerField
                    parseInputfromUser(command)
                    controllerGame = RestUtil.requestControllerGame
                    controllerField = RestUtil.requestControllerField
                    if (controllerGame.board == "Playing") {
                        fieldWithGameStateToHTML(fieldToHTML(controllerField), controllerGame.board)
                    } else {
                        fieldWithGameStateToHTML(fieldToHTML(/* controller.field.gameOverField */FieldTui(controllerField.hidden, controllerField.hidden)), controllerGame.board)
                    }
                } 
            } 
        } ~
        put {
            path("putEvent") {
                    parameter("event".as[String]) { (event) =>
                        //
                        controllerGame = RestUtil.requestControllerGame
                        controllerField = RestUtil.requestControllerField
                        event match
                            case "NewGame" => infoMessages(controllerField.toString()); true
                            case "Start" |"Next" | "Load "=> infoMessages(controllerField.toString()); true
                            case "GameOver" => infoMessages(s"The Game is ${controllerGame.board} !", controllerField.toString()); true
                            case "Exit" => System.exit(0); false
                            case _ => false
                            
                        complete(HttpEntity(ContentTypes.`application/json`, event))
                    }
            }
        }
        
    }

    def start(): Unit = {
        val bindFuture = Http().newServerAt("0.0.0.0", 9080).bind(route)

        bindFuture.onComplete {
            case Success(binding) =>
                println("Server online at http://localhost:9080/")
                complete(binding.toString)
            case Failure(exception) =>
                println(s"An error occurred: $exception")
                complete("fail binding")
        }
    }

/*    val bindFuture = Http().bindAndHandle(route, "localhost", 9080)

    def unbind = bindFuture
        .flatMap(_.unbind())
        .onComplete(_ => system.terminate())*/


    def userIn(rawInput: String): Unit = {
        val firstMoveCheck = false
        rawInput.split("\\s+").toList match {
            case command :: xStr :: yStr :: Nil =>
            (command, xStr.toIntOption, yStr.toIntOption) match {
                case ("o", Some(x), Some(y)) => RestUtil.requestControllerMakeAndPublishDoMove(firstMoveCheck, Move("open", x, y), controllerGame)/*controller.makeAndPublish(controller.doMove, firstMoveCheck, Move("open", x, y),  controller.game)*/
                case ("f", Some(x), Some(y)) => RestUtil.requestControllerMakeAndPublishPut(Move("flag", x, y))/*controller.makeAndPublish(controller.put, Move("flag", x, y))*/
                case ("u", Some(x), Some(y)) => RestUtil.requestControllerMakeAndPublishPut(Move("unflag", x, y))/*controller.makeAndPublish(controller.put, Move("unflag", x, y))*/
                case _ => println("Invalid Input Format") // Replace with your error handling
            }
            case _ => println("Invalid Input Format")
        }
    }


    def fieldToHTML(input:String): StandardRoute =  {
        complete(HttpEntity(ContentTypes.`text/html(UTF-8)`,input))
    }

    def gameOverFieldHtml: StandardRoute =  {
        complete(HttpEntity(ContentTypes.`text/html(UTF-8)`,fieldToHTML(FieldTui(controllerField.hidden, controllerField.hidden)/* controller.field.gameOverField */)))
    }

    def fieldToHtmlNew: StandardRoute =  {
        RestUtil.requestControllerField
        complete(HttpEntity(ContentTypes.`text/html(UTF-8)`,fieldToHTML(controllerField)))
    }

    def fieldWithGameStateToHTML(input:String, additionalInfo: String): StandardRoute =  {
        val html = new StringBuilder()
        html.append(input)
        html.append("</br>")
        html.append("<p>")
        html.append(additionalInfo)
        html.append("</p>")
        complete(HttpEntity(ContentTypes.`text/html(UTF-8)`,html.toString()))
    }

    def infoMessages(text: String*) = {text.foreach(println)}

    def update(e: Event): Boolean =
        e match
            case Event.NewGame => infoMessages(controllerField.toString()); true
            case Event.Start | Event.Next | Event.Load => infoMessages(controllerField.toString()); true
            case Event.GameOver => infoMessages(s"The Game is ${controllerGame.board} !", controllerField.toString()); true
            case Event.Exit => System.exit(0); false
            case _ => false


    def parseInputfromUser(command: String): Unit = {
    infoMessages("Enter your move (<action><x><y>, eg. o0102, q to quit, h for help):")

    userInX(command) match
        case None =>
        case Some(move) => 
            move.value match {
                case "open" => RestUtil.requestControllerMakeAndPublishDoMove(false, move, controllerGame) /*controller.makeAndPublish(controller.doMove, false, move, controller.game)*/
                case "flag" => RestUtil.requestControllerMakeAndPublishPut(move) /*controller.makeAndPublish(controller.put, move)*/
                case "unflag" => RestUtil.requestControllerMakeAndPublishPut(move) /*controller.makeAndPublish(controller.put, move)*/
                case "help" => requestControllerHelpMenue
                case _ => infoMessages(">> Invalid Input")
        }

        requestCheckGameOver(controllerGame.board) match {
            case false => 
            case true => requestGameOver
        }
    }
        


    def userInX(rawInput: String): Option[Move] = {
        val cleanInputPattern: Regex = """^[a-z]{1}[0-9]{4}$""".r
        val onlyOneStringPattern: Regex = """^[q|h|r|z|y|o|f|s|l|u|t]{1}$""".r

        val input = rawInput match
            case cleanInputPattern() => rawInput
            case onlyOneStringPattern() => rawInput
            case _ => infoMessages(">> Invalid Input Format");"e"
        
        input match
            case "q" => System.exit(0); None
            case "h" => requestControllerHelpMenue; None
            case "r" => RestUtil.requestControllerCheat/*controller.cheat*/; None
            case "z" => RestUtil.requestControllerMakeAndPublishUndo/*controller.makeAndPublish(controller.undo)*/; None
            case "y" => RestUtil.requestControllerMakeAndPublishRedo/*controller.makeAndPublish(controller.redo)*/; None
            case "s" => RestUtil.requestControllerSaveGame/*controller.saveGame*/; None
            case "l" => RestUtil.requestControllerLoadGame/*controller.loadGame*/; None
            case "e" => None
            case _ => {
                val charAccumulator = input.toCharArray()
                
                val action = charAccumulator(0) match
                    case 'o' => "open"
                    case 'f' => "flag"
                    case 'u' => "unflag"
                    case _ => "e"
                
                val coordinates = charArrayToInt(charAccumulator) match{
                    case Success(i) => Some(i)
                    case Failure(e) => infoMessages(s">> Invalid Move: ${e.getMessage}"); None
                }

                controllerGame = RestUtil.requestControllerGame

                val validCoordinates: Option[Move] = coordinates match {
                    case Some(i) => {if controllerGame.side > i._1 && controllerGame.side > i._2 then Some(Move(action, i._1, i._2)) else { infoMessages(">> Invalid Move: Coordinates out of bounds"); None}}
                    case None => None
                }
                validCoordinates
            }
        
    }

    def charArrayToInt(s: Array[Char]): Try[(Int, Int)] = Try(((s(1).toString + s(2).toString).toInt, (s(3).toString + s(4).toString).toInt))

    def fieldToHTML(controllerField: FieldTui): String = {
        val matrix = controllerField.matrix
        val size = controllerField.matrix.rows.size
        val html = new StringBuilder
        html.append("<table>")
        for (row <- 0 until size) {
            html.append("<tr>")
            for (col <- 0 until size) {
                html.append("<td>")
                html.append(matrix.rows(row)(col))
                html.append("</td>")
            }
            html.append("</tr>")
        }
        html.append("</table>")
        html.toString()
    }

    def requestControllerHelpMenue = {

        val url = "http://controller:9081/controller/helpMenu"
        val request = HttpRequest(
            method = HttpMethods.GET,
            uri = url
        )

        val bodyStringHelpMessage = Await.result(Http().singleRequest(request).flatMap(_.entity.toStrict(5.seconds).map(_.data.utf8String)), 5.seconds)
        infoMessages(">> Help Menu in WebGuiApi:", bodyStringHelpMessage) // use logging instead
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

    def requestGameOver = {

        val url = "http://controller:9081/controller/gameOver"

        val request = HttpRequest(
            method = HttpMethods.GET,
            uri = url
        )

        //val bodyString = Await.result(Http().singleRequest(request).flatMap(_.entity.toStrict(5.seconds).map(_.data.utf8String)), 5.seconds)
        //val field = jsonToFieldTui(bodyString)
        //implement maybe later controllerfield = field ...
        //controller.gameOver
    }

}*/