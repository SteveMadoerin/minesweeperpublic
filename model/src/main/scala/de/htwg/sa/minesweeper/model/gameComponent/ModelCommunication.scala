package de.htwg.sa.minesweeper.model.gameComponent

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpEntity, HttpRequest, HttpResponse}
import akka.http.scaladsl.server.Directives.{entity, *}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.scaladsl.GraphDSL.Builder
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, Merge, Sink, Source}
import akka.stream.{FlowShape, UniformFanInShape, UniformFanOutShape}
import de.htwg.sa.minesweeper.model.gameComponent.ModelResponseConsumer.ModelResponseConsumer
import de.htwg.sa.minesweeper.model.gameComponent.gameBaseImpl.{Decider, Game, Playfield}
import play.api.libs.json.Json

import java.util.concurrent.TimeUnit
import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success}

class ModelCommunication(using var game: IGame, var field: IField){

    implicit val system: ActorSystem = ActorSystem()
    implicit val executionContext: ExecutionContextExecutor = system.dispatcher
    
    val ModelResponseTopic = "model-response"
    val ControllerResponseTopic = "controller-response"
    val ModelCommandTopic = "model-command"
    val ControllerCommandTopic = "controller-command"
    
    val gameCommandProducer = new ModelCommandProducer(system)
    val gameCommandConsumer = new ModelCommandConsumer(system)
    
    gameCommandConsumer.startConsuming()
    
/*    gameCommandProducer.sendCommand("/model/game", ModelCommandTopic)
    gameCommandProducer.sendCommand("irgendwas", ModelCommandTopic)*/
    
    val gameResponseProducer = new ModelResponseProducer(system)
    val gameResponseConsumer = new ModelResponseConsumer(system)
    
    gameResponseConsumer.startConsuming()
    
    gameResponseProducer.sendResponse("Response from Model", ControllerResponseTopic)
    gameResponseProducer.sendResponse("irgendwas", ControllerResponseTopic)
    
    
    
    
    
    
    

    def createField(leGame: IGame): IField = {
        val adjacentField = Playfield()
        val tempGame: Game = leGame.asInstanceOf[Game]
        adjacentField.newField(leGame.side, tempGame)
    }

    val decider = new Decider()

    private val modelFlow: Flow[HttpRequest, String, NotUsed] = Flow.fromGraph(GraphDSL.create() { implicit builder: Builder[NotUsed] =>
        import GraphDSL.Implicits.*

        val broadcast: UniformFanOutShape[HttpRequest, HttpRequest] = builder.add(Broadcast[HttpRequest](2))
        val merge: UniformFanInShape[String, String] = builder.add(Merge[String](2))


        val commander = Flow[HttpRequest].map { request =>
            request.uri.path.toString match {
                case "/model/game" =>
                    val game = Game(10, 9, 0, "Playing")
                    HttpResponse(entity = game.gameToJson.toString())
                case "/model/game/helpMessage" =>
                    val message = game.helpMessage
                    HttpResponse(entity = message)
                case path if path.startsWith("/model/game/new") =>
                    val queryString = request.uri.query().toString()
                    val params = queryString.split("&")
                    val bombs = params(0).split("=")(1).toInt
                    val size = params(1).split("=")(1).toInt
                    val time = params(2).split("=")(1).toInt
                    this.game = Game(bombs, size, time, "Playing")
                    HttpResponse(entity = game.gameToJson.toString())
                case path if path.startsWith("/model/game/checkExit") =>
                    val queryString = request.uri.query().toString()
                    val params = queryString.split("&")
                    val board = params(0).split("=")(1)
                    val boolean = game.checkExit(board)
                    HttpResponse(entity = boolean.toString())
                case path if path.startsWith("/model/field/new") =>
                    val queryString = request.uri.query().toString()
                    val params = queryString.split("&")
                    val bombs = params(0).split("=")(1).toInt
                    val size = params(1).split("=")(1).toInt
                    val time = params(2).split("=")(1).toInt
                    val spiel = Game(bombs, size, time, "Playing")
                    this.game = spiel
                    val feld = createField(spiel)
                    this.field = feld
                    HttpResponse(entity = feld.fieldToJson)
                case "/model/field" =>
                    HttpResponse(entity = field.fieldToJson.toString())
            }
        }

        val getModelFlow = Flow[HttpRequest].map { request =>
            request.uri.path.toString match {
                case "/model/game" =>
                    val game = Game(10, 9, 0, "Playing")
                    HttpResponse(entity = game.gameToJson.toString())
                case "/model/game/helpMessage" =>
                    val message = game.helpMessage
                    HttpResponse(entity = message)
                case path if path.startsWith("/model/game/new") =>
                    val queryString = request.uri.query().toString()
                    val params = queryString.split("&")
                    val bombs = params(0).split("=")(1).toInt
                    val size = params(1).split("=")(1).toInt
                    val time = params(2).split("=")(1).toInt
                    this.game = Game(bombs, size, time, "Playing")
                    HttpResponse(entity = game.gameToJson.toString())
                case path if path.startsWith("/model/game/checkExit") =>
                    val queryString = request.uri.query().toString()
                    val params = queryString.split("&")
                    val board = params(0).split("=")(1)
                    val boolean = game.checkExit(board)
                    HttpResponse(entity = boolean.toString())
                case path if path.startsWith("/model/field/new") =>
                    val queryString = request.uri.query().toString()
                    val params = queryString.split("&")
                    val bombs = params(0).split("=")(1).toInt
                    val size = params(1).split("=")(1).toInt
                    val time = params(2).split("=")(1).toInt
                    val spiel = Game(bombs, size, time, "Playing")
                    this.game = spiel
                    val feld = createField(spiel)
                    this.field = feld
                    HttpResponse(entity = feld.fieldToJson)
                case "/model/field" =>
                    HttpResponse(entity = field.fieldToJson.toString())
            }
        }

        val getRequestFlowShape = builder.add(getModelFlow)

        val getResponseFlow = Flow[HttpResponse].mapAsync(1) { response =>
            Unmarshal(response.entity).to[String]
        }

        val getResponseFlowShape = builder.add(getResponseFlow)

        val putModelFlow = Flow[HttpRequest].mapAsync(1) { request =>
            request.uri.path.toString match {
                case path if path.startsWith("/model/field/decider") =>
                    val queryString = request.uri.query().toString()
                    val params = queryString.split("&")
                    val b = params(0).split("=")(1).toBoolean
                    val x = params(1).split("=")(1).toInt
                    val y = params(2).split("=")(1).toInt
                    val bombs = params(3).split("=")(1).toInt
                    val size = params(4).split("=")(1).toInt
                    val time = params(5).split("=")(1).toInt
                    val board = params(6).split("=")(1).toInt

                    request.entity.toStrict(Duration.apply(3, TimeUnit.SECONDS)).map { entity =>
                        // doMove
                        val newBoard = board match {
                            case 0 => "Playing"
                            case 1 => "Won"
                            case 2 => "Lost"
                        }
                        val requestBody = entity.data.utf8String
                        val jasonStringField = requestBody
                        val controllerField = field.jsonToField(jasonStringField)
                        val controllerGame = Game(bombs, size, time, newBoard)
                        val (tempGame, tempField): (IGame, IField) = decider.evaluateStrategy(b, x, y, controllerField, controllerGame)
                        val jsonGame = Json.parse(tempGame.gameToJson)
                        val jsonField = Json.parse(field.fieldToJson(tempField))
                        val jsonGameFieldArray = Json.arr(jsonGame, jsonField)

                        HttpResponse(entity = jsonGameFieldArray.toString)
                    }

                case path if path.startsWith("/model/field/showInvisibleCell") =>
                    val queryString = request.uri.query().toString()
                    val params = queryString.split("&")
                    val y = params(0).split("=")(1).toInt
                    val x = params(1).split("=")(1).toInt

                    request.entity.toStrict(Duration.apply(3, TimeUnit.SECONDS)).map { entity =>
                        val requestBody = entity.data.utf8String
                        val jasonStringField = requestBody
                        val fieldFromController = field.jsonToField(jasonStringField)
                        val symbol = fieldFromController.showInvisibleCell(y, x)
                        HttpResponse(entity = Json.parse(symbol).toString())
                    }


                case path if path.startsWith("/model/field/put/flag") =>
                    val queryString = request.uri.query().toString()
                    val params = queryString.split("&")
                    val x = params(0).split("=")(1).toInt
                    val y = params(1).split("=")(1).toInt

                    request.entity.toStrict(Duration.apply(3, TimeUnit.SECONDS)).map { entity =>
                        val requestbody = entity.data.utf8String
                        val jasonStringField = requestbody
                        val fieldFromController = field.jsonToField(jasonStringField)
                        val flaggedField = fieldFromController.putFlag(x, y)
                        val flaggedJsonField = fieldFromController.fieldToJson(flaggedField)
                        HttpResponse(entity = Json.parse(flaggedJsonField).toString())
                    }


                case path if path.startsWith("/model/field/put/removeFlag") =>
                    val queryString = request.uri.query().toString()
                    val params = queryString.split("&")
                    val x = params(0).split("=")(1).toInt
                    val y = params(1).split("=")(1).toInt
                    request.entity.toStrict(Duration.apply(3, TimeUnit.SECONDS)).map { entity =>
                        val requestbody = entity.data.utf8String
                        val jasonStringField = requestbody
                        val fieldFromController = field.jsonToField(jasonStringField)
                        val flaggedField = fieldFromController.removeFlag(x, y)
                        val flaggedJsonField = fieldFromController.fieldToJson(flaggedField)
                        HttpResponse(entity = Json.parse(flaggedJsonField).toString())
                    }


                case path if path.startsWith("/model/field/put") =>
                    val queryString = request.uri.query().toString()
                    val params = queryString.split("&")
                    val symbol = params(0).split("=")(1)
                    val x = params(1).split("=")(1).toInt
                    val y = params(2).split("=")(1).toInt
                    request.entity.toStrict(Duration.apply(3, TimeUnit.SECONDS)).map { entity =>
                        val requestbody = entity.data.utf8String
                        val jasonStringField = requestbody
                        val fieldFromController = field.jsonToField(jasonStringField)
                        val flaggedField = fieldFromController.put(symbol, x, y)
                        val flaggedJsonField = fieldFromController.fieldToJson(flaggedField)
                        HttpResponse(entity = Json.parse(flaggedJsonField).toString())
                    }


                case path if path.startsWith("/model/field/recursiveOpen") =>
                    val queryString = request.uri.query().toString()
                    val params = queryString.split("&")
                    val x = params(0).split("=")(1).toInt
                    val y = params(1).split("=")(1).toInt
                    request.entity.toStrict(Duration.apply(3, TimeUnit.SECONDS)).map { entity =>
                        val requestbody = entity.data.utf8String
                        val jasonStringField = requestbody
                        val saveControllerField = field.jsonToField(jasonStringField)
                        val saveField = saveControllerField.recursiveOpen(x, y, saveControllerField)
                        val openedField = saveField.fieldToJson(saveField)
                        HttpResponse(entity = Json.parse(openedField).toString())
                    }


                case "/model/field/gameOverField" =>
                    request.entity.toStrict(Duration.apply(3, TimeUnit.SECONDS)).map { entity =>
                        val requestbody = entity.data.utf8String
                        val jasonStringField = requestbody
                        // optimization after gatling test
                        val newJsonString = updateMatrixWithHidden(jasonStringField)
                        println("field/gameOverField")
                        HttpResponse(entity = Json.parse(newJsonString).toString())
                    }

                case "/model/field/toString" =>
                    request.entity.toStrict(Duration.apply(3, TimeUnit.SECONDS)).map { entity =>
                        val requestbody = entity.data.utf8String
                        val jasonStringField = requestbody
                        val fieldFromController = field.jsonToField(jasonStringField)
                        val saveField = fieldFromController.toString
                        HttpResponse(entity = saveField)
                    }


                case "/model/field/cheat" =>
                    request.entity.toStrict(Duration.apply(3, TimeUnit.SECONDS)).map { entity =>
                        val requestbody = entity.data.utf8String
                        val jasonStringField = requestbody
                        val fieldFromController = field.jsonToField(jasonStringField)
                        val saveField = fieldFromController.reveal
                        val prepareJsonField = saveField.toString
                        HttpResponse(entity = prepareJsonField)
                    }
                case path if path.startsWith("/model/game/newGameField") =>
                    val queryString = request.uri.query().toString()
                    val params = queryString.split("&")
                    val optionString = params(0).split("=")(1)
                    request.entity.toStrict(Duration.apply(3, TimeUnit.SECONDS)).map { entity =>
                        val requestbody = entity.data.utf8String
                        val jasonStringGame = requestbody
                        val saveControllerGame: Game = game.jsonToGame(jasonStringGame).asInstanceOf[Game]

                        val convertedOptionString = optionString match {
                            case "0" => "SuperEasy"
                            case "1" => "Easy"
                            case "2" => "Medium"
                            case "3" => "Hard"
                            case _ => "Easy"
                        }
                        val controllerOptionString = Some(convertedOptionString)
                        val (feldy, spiely) = saveControllerGame.prepareBoard2(controllerOptionString)(saveControllerGame)

                        val jsonGame = Json.parse(spiely.gameToJson)
                        val jsonField = Json.parse(feldy.fieldToJson(feldy))
                        val jsonGameFieldArray = Json.arr(jsonGame, jsonField)
                        HttpResponse(entity = jsonGameFieldArray.toString)
                    }
            }
        }

        val putRequestFlowShape = builder.add(putModelFlow)

        val putResponseFlow = Flow[HttpResponse].mapAsync(1) { response =>
            Unmarshal(response.entity).to[String]
        }

        val putResponseFlowShape = builder.add(putResponseFlow)

        broadcast.out(0) ~> getRequestFlowShape ~> getResponseFlowShape ~> merge.in(0)
        broadcast.out(1) ~> putRequestFlowShape ~> putResponseFlowShape ~> merge.in(1)

        FlowShape(broadcast.in, merge.out)
    })

    def updateMatrixWithHidden(jsonString: String): String = {
        // Find the start and end indices of the "matrix" and "hidden" arrays
        val matrixStart = jsonString.indexOf("\"matrix\": [") + "\"matrix\": [".length
        val matrixEnd = jsonString.indexOf("]", matrixStart)
        val hiddenStart = jsonString.indexOf("\"hidden\": [") + "\"hidden\": [".length
        val hiddenEnd = jsonString.indexOf("]", hiddenStart)

        val matrixString = jsonString.substring(matrixStart, matrixEnd)
        val hiddenString = jsonString.substring(hiddenStart, hiddenEnd)

        val newJsonString = jsonString.substring(0, matrixStart) + hiddenString + jsonString.substring(matrixEnd)
        newJsonString
    }


/*    def start(): Unit = {
        val bindFuture = Http().newServerAt("0.0.0.0", 9082).bind(
            pathPrefix("model") {
                extractRequest { request =>
                    complete(
                        Source.single(request).via(modelFlow).runWith(Sink.head).map(resp => resp)
                    )
                }
            }
        )
        bindFuture.onComplete {
            case Success(binding) =>
                println("Server online at http://localhost:9082/")
                complete(binding.toString)
            case Failure(exception) =>
                println(s"An error occurred: $exception")
                complete("fail binding")
        }
    }*/

}