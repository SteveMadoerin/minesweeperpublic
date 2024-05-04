package de.htwg.sa.minesweeper.ui


import de.htwg.sa.minesweeper.controller.controllerComponent.IController
import de.htwg.sa.minesweeper.util.{Observer, Move, Event}

import scala.io.StdIn.readLine
import scala.util.{Try, Success, Failure}

import scala.compiletime.ops.string
import scala.util.matching.Regex

import play.api.libs.json.{JsValue, Json}
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

// TODO replace controller with API calls
class TUI(using var controller: IController):
    
    //controller.add(this) // TODO: implement
    implicit val system: ActorSystem = ActorSystem()
    implicit val materializer: Materializer = Materializer(system)
    implicit val executionContext: ExecutionContextExecutor = system.dispatcher

    // using var controller: IController


    def run = 
        infoMessages("Welcome to Minesweeper")
        resize
        parseInputandPrintLoop(firstMoveCheck = true)
        
    def update(e: Event): Boolean = 
        e match
            case Event.NewGame | Event.Start | Event.Next | Event.Load => infoMessages(controller.field.toString()); true
            case Event.GameOver => infoMessages(s"The Game is ${controller.game.board} !", controller.field.toString()); true
            case Event.Exit => System.exit(0); false
            case Event.Help => /* requestControllerHelpMenue; */infoMessages(requestControllerFieldToString); true
            case _ => false
    
    def userInX(rawInput: String): Option[Move] = {
        val cleanInputPattern: Regex = """^[a-z]{1}[0-9]{4}$""".r
        val onlyOneStringPattern: Regex = """^[q|h|r|z|y|o|f|s|l|u|t]{1}$""".r

        val input = rawInput match
            case cleanInputPattern() => rawInput
            case onlyOneStringPattern() => rawInput
            case _ => infoMessages(">> Invalid Input Format");"e"
        
        input match
            case "q" => System.exit(0); None
            case "h" => requestControllerHelpMenue; None // TODO implement help menu
            case "r" => requestControllerCheat; None
            case "z" => requestControllerMakeAndPublishUndo; None
            case "y" => requestControllerMakeAndPublishRedo; None
            case "s" => requestControllerSaveGame; None
            case "l" => requestControllerLoadGame; None
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

    // recursion
    def parseInputandPrintLoop(firstMoveCheck: Boolean): Unit = {
        infoMessages("Enter your move (<action><x><y>, eg. o0102, q to quit, h for help):")
        val stillFirstMove = userInX(readLine) match {
            case None => firstMoveCheck
            case Some(move) =>
                processMove(move, firstMoveCheck)
                false
        }
        
        controller.checkGameOver(controller.game.board) match {
            case false =>
                parseInputandPrintLoop(stillFirstMove)
            case true =>
                controller.gameOver
                restart
        }
    }
    

    def processMove(move: Move, firstMoveCheck: Boolean): Boolean = {
        move.value match {
            case "open" => controller.makeAndPublish(controller.doMove, firstMoveCheck, move, controller.game); true
            case "flag" => controller.makeAndPublish(controller.put, move); true
            case "unflag" => controller.makeAndPublish(controller.put, move); true
            case "help" => controller.helpMenu; true
            case _ => infoMessages(">> Invalid Input"); false
        }
    }
    
    def restart = 
        infoMessages("Do you want to play again? (yes/no)")
        readLine match
            case "yes" => run
            case "no" => controller.exit
            case _ => infoMessages(">> Invalid Input");
    
    def charArrayToInt(s: Array[Char]): Try[(Int, Int)] = Try(((s(1).toString + s(2).toString).toInt, (s(3).toString + s(4).toString).toInt))
    
    def resize: Unit = 
        val (side, bombs) = chooseDifficulty()
        controller.newGame(side, bombs)
    
    def chooseDifficulty() = {
        val multilineString = 
            """Enter the Difficulty Level
            |Press 0 for SUPEREASY (5 * 5 Cells and 1 Mine)
            |Press 1 for BEGINNER (9 * 9 Cells and 10 Mines)
            |Press 2 for INTERMEDIATE (15 * 15 Cells and 40 Mines)
            |Press 3 for ADVANCED (19 * 19 Cells and 85 Mines)""".stripMargin

        infoMessages(multilineString)

        val ebene = scala.io.StdIn.readLine
        val level = Try(ebene.toInt).getOrElse(1)
        level match {
            case 0 => (5, 5)
            case 1 => (9, 10)
            case 2 => (15, 40)
            case 3 => (19, 85)
            case _ => (9, 10)
        }
    }

    def infoMessages(text: String*) = {
        text.foreach(println)
    }

    def requestControllerHelpMenue= {

        val url = "http://localhost:8081/controller/helpMenu"
        val request = HttpRequest(
            method =  HttpMethods.GET,
            uri = url
        )

        val bodyStringHelpMessage = Await.result(Http().singleRequest(request).flatMap(_.entity.toStrict(5.seconds).map(_.data.utf8String)), 5.seconds)
        infoMessages(">> Help Menu in TUI:", bodyStringHelpMessage)
        update(Event.Help)
        //controller.helpMenu
        //println(// controller.field.toString)
    }

    def requestControllerCheat: Unit = {
        controller.cheat
    }

    def requestControllerMakeAndPublishUndo: Unit = {
        controller.makeAndPublish(controller.undo)
    }

    def requestControllerMakeAndPublishRedo: Unit = {
        controller.makeAndPublish(controller.redo)
    }

    def requestControllerSaveGame: Unit = {
        controller.saveGame
    }

    def requestControllerLoadGame: Unit = {
        controller.loadGame
    }

    def requestControllerFieldToString: String = {
        val url = "http://localhost:8081/controller/field/toString"

        val request = HttpRequest(
            method =  HttpMethods.GET,
            uri = url
        )

        val responseFuture: Future[HttpResponse] = Http().singleRequest(request)
        val bodyStringFuture: Future[String] = responseFuture.flatMap { response =>
            response.entity.toStrict(5.seconds).map(_.data.utf8String)
        }
        val bodyStringField = Await.result(bodyStringFuture, 5.seconds)
        bodyStringField
        /* infoMessages(controller.field.toString()) */
        
    }

    val route: Route = {
        get {
            path("tui") {
                complete("TUI")
            } ~ 
            path("tui"/"hello") {
                complete("hello")
            } 
        } ~
        put {
            path("tui"/"notify") {
                    parameter("event".as[String]) { (event) =>
                        //
                        event match
                            case "NewGame" => update(Event.NewGame)
                            case "Start" => update(Event.Start)
                            case "Next" => update(Event.Next)
                            case "GameOver" => update(Event.GameOver)
                            case "Cheat" => update(Event.Cheat)
                            case "Help" => update(Event.Help)
                            case "Input" => update(Event.Input)
                            case "Load" => update(Event.Load)
                            case "Save" => update(Event.Save)
                            case "SaveTime" => update(Event.SaveTime)
                            case "Exit" => update(Event.Exit)
                            case _ => false
                            
                        complete("success notify" + event)
                    }
            }
        }
        
    }


    val bindFuture = Http().bindAndHandle(route, "localhost", 8088)

    def unbind = bindFuture
        .flatMap(_.unbind())
        .onComplete(_ => system.terminate())