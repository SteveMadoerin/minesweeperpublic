package de.htwg.sa.minesweeper.ui


import de.htwg.sa.minesweeper.controller.controllerComponent.IController
import de.htwg.sa.minesweeper.util.Observer
import de.htwg.sa.minesweeper.util.Event


import de.htwg.sa.minesweeper.util.Move
import de.htwg.sa.minesweeper.controller.controllerComponent.IController
import de.htwg.sa.minesweeper.util.Observer
import de.htwg.sa.minesweeper.util.Event
import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, Materializer}
import scala.concurrent.ExecutionContext
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.RouteDirectives
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Route, StandardRoute}
import de.htwg.sa.minesweeper.ui.config.Default.{given}
import scala.concurrent.ExecutionContextExecutor
import scala.util.Failure
import scala.util.Success
import scala.util.matching.Regex
import scala.util.Try
import de.htwg.sa.minesweeper.model.gameComponent.gameBaseImpl.Matrix
import de.htwg.sa.minesweeper.model.gameComponent.gameBaseImpl.Field
import de.htwg.sa.minesweeper.model.gameComponent.IField
import de.htwg.sa.minesweeper.entity.FieldDTO


class WebGuiApi(using var controller: IController) extends Observer{

    controller.add(this)

    implicit val system: ActorSystem = ActorSystem()
    implicit val materializer: Materializer = Materializer(system)
    implicit val executionContext: ExecutionContextExecutor = system.dispatcher



    val route: Route = {
        get {
            path("ui") {
                complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>THE MINESWEEPER GAME</h1>"))
            } ~ 
            path("ui"/"new"/"small") {
                controller.newGame(9, 10)
                fieldToHtmlNew
            } ~ 
            path("ui"/"undo") {
                controller.makeAndPublish(controller.undo)
                fieldToHtmlNew
            } ~ 
            path("ui"/"redo") {
                controller.makeAndPublish(controller.redo)
                fieldToHtmlNew
                //fieldToHTML(controller.field.toString)
            } ~ 
            path("minesweeper" / Segment) {
                command => {
                    parseInputfromUser(command)
                    if (controller.game.board == "Playing") {
                        fieldWithGameStateToHTML(fieldToHTML(controller.field), controller.game.board)
                    } else {
                        fieldWithGameStateToHTML(fieldToHTML(/* controller.field.gameOverField */FieldDTO(controller.field.hidden, controller.field.hidden)), controller.game.board)
                    }
                } 
            } 
        } ~
        put {
            path("putEvent") {
                    parameter("event".as[String]) { (event) =>
                        //
                        event match
                            case "NewGame" => infoMessages(controller.field.toString()); true
                            case "Start" |"Next" | "Load "=> infoMessages(controller.field.toString()); true
                            case "GameOver" => infoMessages(s"The Game is ${controller.game.board} !", controller.field.toString()); true
                            case "Exit" => System.exit(0); false
                            case _ => false
                            
                        complete(HttpEntity(ContentTypes.`application/json`, event))
                    }
            }
        }
        
    }


    val bindFuture = Http().bindAndHandle(route, "localhost", 8080)

    def unbind = bindFuture
        .flatMap(_.unbind())
        .onComplete(_ => system.terminate())


    def userIn(rawInput: String): Unit = {
        val firstMoveCheck = false
        rawInput.split("\\s+").toList match {
            case command :: xStr :: yStr :: Nil =>
            (command, xStr.toIntOption, yStr.toIntOption) match {
                case ("o", Some(x), Some(y)) => controller.makeAndPublish(controller.doMove, firstMoveCheck, Move("open", x, y),  controller.game)
                case ("f", Some(x), Some(y)) => controller.makeAndPublish(controller.put, Move("flag", x, y))
                case ("u", Some(x), Some(y)) => controller.makeAndPublish(controller.put, Move("unflag", x, y))
                case _ => println("Invalid Input Format") // Replace with your error handling
            }
            case _ => println("Invalid Input Format") // Replace with your error handling
        }
    }


    def fieldToHTML(input:String): StandardRoute =  {
        complete(HttpEntity(ContentTypes.`text/html(UTF-8)`,input))
    }

    def gameOverFieldHtml: StandardRoute =  {
        complete(HttpEntity(ContentTypes.`text/html(UTF-8)`,fieldToHTML(FieldDTO(controller.field.hidden, controller.field.hidden)/* controller.field.gameOverField */)))
    }

    def fieldToHtmlNew: StandardRoute =  {
        complete(HttpEntity(ContentTypes.`text/html(UTF-8)`,fieldToHTML(controller.field)))
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

    override def update(e: Event): Boolean = 
        e match
            case Event.NewGame => infoMessages(controller.field.toString()); true
            case Event.Start | Event.Next | Event.Load => infoMessages(controller.field.toString()); true
            case Event.GameOver => infoMessages(s"The Game is ${controller.game.board} !", controller.field.toString()); true
            case Event.Exit => System.exit(0); false
            case _ => false


    def parseInputfromUser(command: String): Unit = {
    infoMessages("Enter your move (<action><x><y>, eg. o0102, q to quit, h for help):")

    userInX(command) match
        case None =>
        case Some(move) => 
            move.value match {
                case "open" => controller.makeAndPublish(controller.doMove, false, move, controller.game)
                case "flag" => controller.makeAndPublish(controller.put, move)
                case "unflag" => controller.makeAndPublish(controller.put, move)
                case "help" => controller.helpMenu
                case _ => infoMessages(">> Invalid Input")
        }

        controller.checkGameOver(controller.game.board) match {
            case false => 
            case true => controller.gameOver
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
            case "h" => controller.helpMenu; None
            case "r" => controller.cheat; None
            case "z" => controller.makeAndPublish(controller.undo); None
            case "y" => controller.makeAndPublish(controller.redo); None
            case "s" => controller.saveGame; None
            case "l" => controller.loadGame; None
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

                val validCoordinates: Option[Move] = coordinates match {
                    case Some(i) => {if controller.game.side > i._1 && controller.game.side > i._2 then Some(Move(action, i._1, i._2)) else { infoMessages(">> Invalid Move: Coordinates out of bounds"); None}} // no var game
                    case None => None
                }
                validCoordinates
            }
        
    }

    def charArrayToInt(s: Array[Char]): Try[(Int, Int)] = Try(((s(1).toString + s(2).toString).toInt, (s(3).toString + s(4).toString).toInt))

    def fieldToHTML(controllerField: FieldDTO): String = {
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

}
