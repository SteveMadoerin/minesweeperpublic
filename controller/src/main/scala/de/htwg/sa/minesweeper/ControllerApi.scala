package de.htwg.sa.minesweeper

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.server.{PathMatcher, PathMatcher1, Route}

import java.net.{HttpURLConnection, URL}
import scala.concurrent.ExecutionContext
import de.htwg.sa.minesweeper.util.Observer
import de.htwg.sa.minesweeper.controller.controllerComponent.IController
import de.htwg.sa.minesweeper.util.RestUtil
import de.htwg.sa.minesweeper.util.Event
import akka.stream.Materializer
import scala.concurrent.ExecutionContextExecutor


class ControllerApi(using var controller: IController) extends Observer:
    controller.add(this)
    
    
    implicit val system: ActorSystem = ActorSystem()
    implicit val materializer: Materializer = Materializer(system)
    implicit val executionContext: ExecutionContextExecutor = system.dispatcher


    // update TUI and GUI when an event occurs ->
    @Override def update(e: Event): Boolean = 
        e match
            case Event.NewGame | Event.Start | Event.Next | Event.Load => infoMessages(controller.field.toString()); true
            case Event.GameOver => infoMessages(s"The Game is ${controller.game.board} !", controller.field.toString()); true
            case Event.Exit => System.exit(0); false
            case _ => false

    def infoMessages(text: String*) = {
        text.foreach(println)
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
                complete(controller.toString()/* controller.toJson.toString */)
              },
              path("field") {
                complete(RestUtil.fieldToJson(controller.field).toString)
              },
              path("field"/"toString") {
                complete(controller.fieldToString)
              },
              path("game") {
                complete(RestUtil.gameToJson(controller.game).toString)
              },
              path("helpMenu") {
                controller.helpMenu // this also notifies the observer
                complete(controller.helpMenuRest)
              },
              path("loadGame") {
                controller.loadGame
                complete("controller.loadGame")
              },
              path("next") {
                complete("next"/* controller.next() */)
              },
              pathPrefix("makeAndPublish") {
                concat(
                  path("doMove") {
                    complete("controller.makeAndPublish(controller.doMove(b, move, game, field))" ) // "open"
                  },
                  path("put") {
/*                     val c = controller.makeAndPublish(controller.put, move) // flg or unflag
                    print(c) */
                    complete("c")
                  },
                  path("undo") {
                      complete("controller.makeAndPublish(controller.undo)")
                  },
                  path("redo") {
                      complete("controller.makeAndPublish(controller.redo)")
                  }
                )
              },
              path("saveGame") {
                controller.saveGame
                complete("successfully saved")
              },
              path("undo") {
                controller.undo
                complete(RestUtil.fieldToJson(controller.field).toString)
              },
              path("redo") {
                controller.redo
                complete(RestUtil.fieldToJson(controller.field).toString)
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
              path("quit") {
                complete("controller.quit()")
              },
              path("notify") {
                // 
                complete("event")
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



end ControllerApi