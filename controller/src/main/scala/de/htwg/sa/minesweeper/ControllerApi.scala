/* package de.htwg.sa.minesweeper

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.server.{PathMatcher, PathMatcher1, Route}

import java.net.{HttpURLConnection, URL}
import scala.concurrent.ExecutionContext
import de.htwg.sa.minesweeper.util.Observer
import de.htwg.sa.minesweeper.controller.controllerComponent.IController


final case class ControllerApi(controller: IController) extends Observer:
    controller.add(this)
    
    implicit val system: ActorSystem = ActorSystem()
    implicit val executionContext: ExecutionContext = system.dispatcher
    
    Http().newServerAt("localhost", 9006).bind(
    concat(
      pathPrefix("controller") {
        concat(
          get {
            concat(
              pathSingleSlash {
                complete(controller.toString)
              },
              path("controller") {
                complete(controller.toJson.toString)
              },
              path("field") {
                complete(controller.field.toJson.toString)
              },
              path("game") {
                complete(controller.game.toJson.toString)
              },
              path("diceCup") {
                complete(controller.diceCup.toJson.toString)
              },
              path("loadGame") {
                complete(controller.loadGame)
              },
              path("next") {
                complete(controller.next())
              },
              pathPrefix("makeAndPublish") {
                concat(
                  path("doMove") {
                    complete(controller.makeAndPublish(controller.doMove(b, move, game, field))) // "open"
                  },
                  path("put") {
                    val c = controller.makeAndPublish(controller.put, move) // falg or unflag
                    print(c)
                    complete(c)
                  },
                  path("undo") {
                    /* (pi: List[Int]) => */
                      complete(controller.makeAndPublish(controller.undo))
                  },
                  path("redo") {
                    /* (po: List[Int]) => */
                      complete(controller.makeAndPublish(controller.redo))
                  }
                )
              },
              path("saveGame") {
                complete(controller.saveGame)
              },
              path("undo") {
                complete(controller.undo())
              },
              path("redo") {
                complete(controller.redo())
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
          post {
            concat(
              path("put") {
                entity(as[String]) { requestBody =>
                  complete(controller.put(controller.jsonStringToMove(requestBody)))
                }
              },
              path("quit") {
                complete(controller.quit())
              },
              path("nextRound") {
                complete(controller.nextRound().toJson.toString)
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
 */