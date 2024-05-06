package de.htwg.sa.minesweeper.controller

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{PathMatcher, PathMatcher1, Route}

import java.net.{HttpURLConnection, URL}
import scala.concurrent.ExecutionContext
import de.htwg.sa.minesweeper.util.Observer
import de.htwg.sa.minesweeper.controller.controllerComponent.IController
import de.htwg.sa.minesweeper.util.RestUtil
import de.htwg.sa.minesweeper.util.Event
import akka.stream.Materializer
import scala.concurrent.ExecutionContextExecutor

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

/* import de.htwg.sa.minesweeper.model.gameComponent.gameBaseImpl.Game
import de.htwg.sa.minesweeper.model.gameComponent.gameBaseImpl.Playfield
import de.htwg.sa.minesweeper.model.gameComponent.config.Default
import de.htwg.sa.minesweeper.model.gameComponent.gameBaseImpl.Decider */
import scala.compiletime.ops.boolean
import play.api.libs.json.Json
import de.htwg.sa.minesweeper.util.Move
import play.api.libs.json.JsValue
import de.htwg.sa.minesweeper.entity.GameDTO


class ControllerApi(using var controller: IController) extends Observer:
  controller.add(this)
    
    
  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: Materializer = Materializer(system)
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  @Override def update(e: Event): Boolean = 
      e match
          case Event.Start => true
          case Event.NewGame => true
          case Event.Next => true
          case Event.Load => true
          case Event.GameOver => true
          case Event.Exit => true
          case _ => false

  def infoMessages(text: String*) = {
      text.foreach(println)
  }

  def jsonToMove(json: JsValue): Move = {
    val x = (json \ "x").get.toString.toInt
    val y = (json \ "y").get.toString.toInt
    val action1 = (json \ "value").get.toString
    val action2 = action1.replace("\"", "")
    Move(action2, x, y)
  }

    
  Http().newServerAt("localhost", 8081).bind(
    concat(
      pathPrefix("controller") {
        concat(
          get {
            concat(
              pathSingleSlash {
                complete(controller.toString)
              },
              path("controller") {
                complete(controller.toString())
              },
              path("field") {
                complete(RestUtil.fieldDtoToJson(controller.field).toString)
              },
              path("gameOver") {
                controller.gameOver
                complete(RestUtil.fieldDtoToJson(controller.field).toString)
              },
              path("field"/"toString") {
                complete(controller.fieldToString)
              },
              path("game") {
                complete(RestUtil.gameDtoToJson(controller.game).toString)
              },
              path("helpMenu") {
                controller.helpMenu // this also notifies the observer
                complete(controller.helpMenuRest)
              },
              path("loadGame") {
                controller.loadGame
                complete(controller.fieldToString)
              },
              path("cheat") {
                controller.cheat
                complete(controller.fieldToString)
              },
              pathPrefix("makeAndPublish") {
                concat(
                  path("doMove") {
                    parameter("b".as[Boolean],"bombs".as[Int], "size".as[Int], "time".as[Int], "board".as[Int]) { (b, bombs, size, time, board) =>
                      entity(as[String]) { moveEntitiy =>
                          // doMove
                          val firstMoveCheck = b
                          val newBoard = board.match
                          {
                            case 0 => "Playing"
                            case 1 => "Won"
                            case 2 => "Lost"
                          }
                          val requestBody = Json.parse(moveEntitiy)
                          val move = jsonToMove(requestBody)
                          //controller.makeAndPublish(controller.doMove, firstMoveCheck, move, controller.game)
                          controller.makeAndPublish(controller.doMove, firstMoveCheck, move, GameDTO(bombs, size, time, newBoard))
                          //controller.makeAndPublish(controller.put, move)
                          print("move: " + move.value + " x: " + move.x + " y: " + move.y)

                          complete("success doMove")
                      }
                    }
                  },
                  path("put") {
                    entity(as[String]) { moveEntitiy =>
                      val requestBody = Json.parse(moveEntitiy)

                      val move = jsonToMove(requestBody)
                      controller.makeAndPublish(controller.put, move)
                      print("move: " + move.value + " x: " + move.x + " y: " + move.y)

                      complete("success")
                    }
                  },
                  path("undo") {
                    val returnField = controller.makeAndPublish(controller.undo)
                    complete(controller.fieldToString)
                  },
                  path("redo") {
                    val returnField = controller.makeAndPublish(controller.redo)
                    complete(controller.fieldToString)
                  }
                )
              },
              path("saveGame") {
                controller.saveGame
                complete("successfully saved")
              },
              path("undo") {
                controller.undo
                complete(RestUtil.fieldDtoToJson(controller.field).toString)
              },
              path("redo") {
                controller.redo
                complete(RestUtil.fieldDtoToJson(controller.field).toString)
              },
/*               path("writeDown" / StringValue) {
                (value: String) =>
                  try {
                    val currentPlayer = controller.gameESI.sendPlayerIDRequest(controller.game)
                    val indexOfField = controller.diceCupESI.sendIndexOfFieldRequest(value)
                    val result = controller.diceCupESI.sendResultRequest(indexOfField, controller.diceCup)
                    complete(controller.writeDown(Move(result, currentPlayer, indexOfField)))
                  } catch {
                    case e: Throwable => complete("Invalid Input!")
                  }
              }, */
              path("") {
                sys.error("No such GET route")
              }
            )
          },
          put {
            concat(
              path("put") {
                entity(as[String]) { requestBody =>
                  complete("controller.put(controller.jsonStringToMove(requestBody))")
                }
              },
              path("exit") {
                controller.exit
                complete("exit")
              },
              path("checkGameOver") {
                entity(as[String]) { requestStatus =>
                  val result = controller.checkGameOver(requestStatus)

                  complete(result.toString)
                }
              },
              path("newGame") {
                parameter("bombs".as[Int], "side".as[Int]) { (bombs, side) =>
                  controller.newGame(side, bombs)
                  val feld = controller.field
                  val game = controller.game
                  val jsonField = Json.parse(RestUtil.fieldDtoToJson(feld))
                  val jsonGame = Json.parse(RestUtil.gameDtoToJson(game))

                  val jsonGameFieldArray = Json.arr(jsonGame, jsonField)
                  complete(HttpEntity(ContentTypes.`application/json`, jsonGameFieldArray.toString))

                }
              },
              path("") {
                sys.error("No such POST route")
              }
            )
          }
        )
      }
    )
  )

  def start: Future[Nothing] = Await.result(Future.never, Duration.Inf)



end ControllerApi