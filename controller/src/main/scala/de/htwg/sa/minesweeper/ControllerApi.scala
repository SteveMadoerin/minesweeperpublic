package de.htwg.sa.minesweeper

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.stream.Materializer
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, Merge, Sink, Source}
import akka.stream.{FlowShape, UniformFanInShape, UniformFanOutShape}
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.stream.scaladsl.GraphDSL.Builder
import akka.NotUsed
import de.htwg.sa.minesweeper.controller.controllerComponent.IController
import de.htwg.sa.minesweeper.entity.GameDTO
import de.htwg.sa.minesweeper.util.{Event, Move, Observer, RestUtil}
import play.api.libs.json.{JsValue, Json}

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.Duration
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}


class ControllerApi(using var controller: IController) extends Observer:
    controller.add(this)

    implicit val system: ActorSystem = ActorSystem()
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

    private val controllerFlow: Flow[HttpRequest, String, NotUsed] = Flow.fromGraph(GraphDSL.create() { implicit builder: Builder[NotUsed] =>
        import GraphDSL.Implicits._

        val broadcast: UniformFanOutShape[HttpRequest, HttpRequest] = builder.add(Broadcast[HttpRequest](2))
        val merge: UniformFanInShape[String, String] = builder.add(Merge[String](2))

        val getControllerFlow = Flow[HttpRequest].map { request =>
            request.uri.path.toString match {
                case "/controller/" =>
                    HttpResponse(entity = controller.toString)
                case "/controller/controller" =>
                    HttpResponse(entity = controller.toString())
                case "/controller/field" =>
                    HttpResponse(entity = RestUtil.fieldDtoToJson(controller.field))
                case "/controller/gameOver" =>
                    controller.gameOver
                    HttpResponse(entity = RestUtil.fieldDtoToJson(controller.field))
                case "/controller/field/toString" =>
                    HttpResponse(entity = controller.fieldToString)
                case "/controller/game" =>
                    HttpResponse(entity = RestUtil.gameDtoToJson(controller.game))
                case "/controller/helpMenu" =>
                    controller.helpMenu
                    HttpResponse(entity = controller.helpMenuRest)
                case "/controller/loadGame" =>
                    controller.loadGame
                    HttpResponse(entity = controller.fieldToString)
                case "/controller/newGameGui" =>
                    controller.newGameGUI
                    HttpResponse(entity = "newGameGui")
                case path if path.startsWith("/controller/saveTime") =>
                    val query = request.uri.query()
                    val time = query.get("time").get.toInt
                    controller.saveTime(time)
                    HttpResponse(entity = "Time saved")
                case path if path.startsWith("/controller/saveScoreAndPlayerName") =>
                    val query = request.uri.query()
                    val score = query.get("score").get.toInt
                    val playerName = query.get("playerName").get
                    val emptypath = ""
                    controller.saveScoreAndPlayerName(playerName, score, emptypath)
                    HttpResponse(entity = "score saved")
                case path if path.startsWith("/controller/newGameFieldGui") =>
                    val query = request.uri.query()
                    val input = query.get("input").get
                    val inputOption = Some(input)
                    controller.newGameField(inputOption)
                    HttpResponse(entity = "newGameFieldGui")
                case "/controller/cheat" =>
                    val cheat = controller.cheatRest
                    controller.notifyObserversRest("Cheat")
                    HttpResponse(entity = cheat)

                case path if path.startsWith("/controller/makeAndPublish/doMove") =>
                    val query = request.uri.query()
                    val b = query.get("b").get.toBoolean
                    val bombs = query.get("bombs").get.toInt
                    val size = query.get("size").get.toInt
                    val time = query.get("time").get.toInt
                    val board = query.get("board").get
                    val move = Unmarshal(request.entity).to[String].map { moveEntity =>
                        val requestBody = Json.parse(moveEntity)
                        val move = jsonToMove(requestBody)
                        val firstMoveCheck = b

                        val newBoard = board match {
                            case "0" => "Playing"
                            case "1" => "Won"
                            case "2" => "Lost"
                        }
                        controller.makeAndPublish(controller.doMove, firstMoveCheck, move, GameDTO(bombs, size, time, newBoard))
                    }
                    HttpResponse(entity = "success doMove")
                case path if path.startsWith("/controller/makeAndPublish/put") =>
                    val move = Unmarshal(request.entity).to[String].map { moveEntity =>
                        val requestBody = Json.parse(moveEntity)
                        val move = jsonToMove(requestBody)
                        controller.makeAndPublish(controller.put, move)
                    }
                    HttpResponse(entity = "success")
                case "/controller/makeAndPublish/undo" =>
                    val returnField = controller.makeAndPublish(controller.undo)
                    HttpResponse(entity = controller.fieldToString)
                case "/controller/makeAndPublish/redo" =>
                    val returnField = controller.makeAndPublish(controller.redo)
                    HttpResponse(entity = controller.fieldToString)
                case "/controller/saveGame" =>
                    controller.saveGame
                    HttpResponse(entity = "successfully saved")
                case "/controller/undo" =>
                    controller.undo
                    HttpResponse(entity = RestUtil.fieldDtoToJson(controller.field))
                case "/controller/redo" =>
                    controller.redo
                    HttpResponse(entity = RestUtil.fieldDtoToJson(controller.field))
            }
        }

        val getRequestFlowShape = builder.add(getControllerFlow)

        val getResponseFlow = Flow[HttpResponse].mapAsync(1) { response =>
            Unmarshal(response.entity).to[String]
        }

        val getResponseFlowShape = builder.add(getResponseFlow)

        val putControllerFlow = Flow[HttpRequest].mapAsync(1) { request =>
            request.uri.path.toString match {
                case "/controller/put" =>
                    Future.successful(HttpResponse(entity = "controller.put(controller.jsonStringToMove(requestBody))"))
                case "/controller/exit" =>
                    controller.exit
                    Future.successful(HttpResponse(entity = "exit"))
                case "/controller/checkGameOver" =>
                    request.entity.toStrict(Duration.apply(3, TimeUnit.SECONDS)).map { entity =>
                        val requestbody = entity.data.utf8String
                        val result = controller.checkGameOver(requestbody)
                        print(requestbody + "checkgameover string")
                        HttpResponse(entity = result.toString)
                    }
                case "/controller/checkGameOverGui" =>
                    val result = controller.checkGameOverGui
                    Future.successful(HttpResponse(entity = result.toString))
                case path if path.startsWith("/controller/newGameForGui") =>
                    val query = request.uri.query()
                    val bombs = query.get("bombs").get.toInt
                    val side = query.get("side").get.toInt
                    controller.newGameForGui(side, bombs)
                    val feld = controller.field
                    val game = controller.game
                    val jsonField = Json.parse(RestUtil.fieldDtoToJson(feld))
                    val jsonGame = Json.parse(RestUtil.gameDtoToJson(game))
                    val jsonGameFieldArray = Json.arr(jsonGame, jsonField)
                    Future.successful(HttpResponse(entity = jsonGameFieldArray.toString))
                case path if path.startsWith("/controller/newGame") =>
                    val query = request.uri.query()
                    val side = query.get("side").get.toInt
                    val bombs = query.get("bombs").get.toInt
                    controller.newGame(side, bombs)
                    val feld = controller.field
                    val game = controller.game
                    val jsonField = Json.parse(RestUtil.fieldDtoToJson(feld))
                    val jsonGame = Json.parse(RestUtil.gameDtoToJson(game))
                    val jsonGameFieldArray = Json.arr(jsonGame, jsonField)
                    Future.successful(HttpResponse(entity = jsonGameFieldArray.toString))
            }
        }

        val putRequestFlowShape = builder.add(putControllerFlow)

        val putResponseFlow = Flow[HttpResponse].mapAsync(1) { response =>
            Unmarshal(response.entity).to[String]
        }

        val putResponseFlowShape = builder.add(putResponseFlow)

        broadcast.out(0) ~> getRequestFlowShape ~> getResponseFlowShape ~> merge.in(0)
        broadcast.out(1) ~> putRequestFlowShape ~> putResponseFlowShape ~> merge.in(1)
        
        FlowShape(broadcast.in, merge.out)
    })

    def start(): Unit = {
        val bindFuture = Http().newServerAt("0.0.0.0", 9081).bind(
            pathPrefix("controller") {
                extractRequest { request =>
                    complete(
                        Source.single(request).via(controllerFlow).runWith(Sink.head).map(resp => resp)
                    )
                }
            }
        )

        bindFuture.onComplete {
            case Success(binding) =>
                println("Server online at http://localhost:9081/")
                complete(binding.toString)
            case Failure(exception) =>
                println(s"An error occurred: $exception")
                complete("fail binding")
        }
    }

end ControllerApi