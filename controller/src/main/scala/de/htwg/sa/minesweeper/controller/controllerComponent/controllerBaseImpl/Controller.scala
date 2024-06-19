package de.htwg.sa.minesweeper.controller.controllerComponent.controllerBaseImpl

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.scaladsl.{Flow, GraphDSL, RunnableGraph, Sink, Source}
import akka.stream.{ClosedShape, Materializer}
import de.htwg.sa.minesweeper.{GuiNotificationProducer, TuiNotificationProducer}
import de.htwg.sa.minesweeper.controller.controllerComponent.IController
import de.htwg.sa.minesweeper.entity.{FieldDTO, GameDTO, MatrixDTO}
import de.htwg.sa.minesweeper.util.{Move, Observable, RestUtil, UndoRedoManager}
import play.api.libs.json._

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContextExecutor, Future}
import scala.io.Source
import scala.language.postfixOps
import scala.util.{Failure, Success, Try}



class Controller() extends IController with Observable:
    
    
    implicit val system: ActorSystem = ActorSystem()
    implicit val materializer: Materializer = Materializer(system)
    implicit val executionContext: ExecutionContextExecutor = system.dispatcher
    
    var game: GameDTO = GameDTO(10, 9, 0, "Playing")
    var field: FieldDTO = createFieldDTO(game)

    val undoRedoManager = new UndoRedoManager[FieldDTO]
    
    def createFieldDTO(leGame: GameDTO): FieldDTO = {
        val url = s"http://model:9082/model/field/new?bombs=${leGame.bombs}&size=${leGame.side}&time=${leGame.time}"

        val returnField: FieldDTO = Try {
            val result = scala.io.Source.fromURL(url).mkString
            RestUtil.jsontToFieldDTO(result)
        } match {
            case Success(field) => field
            case Failure(e) =>
                println(s"Error fetching or parsing field data: ${e.getMessage}")
                FieldDTO(MatrixDTO[String](Vector.empty), new MatrixDTO[String](Vector.empty))
            
        }
        returnField
    }


    def doMove(b: Boolean, move: Move, game: GameDTO) = 
        synchronized{            

            val convertedBoard = game.board match {
                case "Playing" => 0
                case "Won" => 1
                case "Lost" => 2
            }

            val url = s"http://model:9082/model/field/decider?b=${b}&x=${move.x}&y=${move.y}&bombs=${game.bombs}&size=${game.side}&time=${game.time}&board=$convertedBoard"
            val jsonFieldFirst = RestUtil.fieldDtoToJson(field)
            val jsonFileContentFirst = jsonFieldFirst.getBytes("UTF-8")

            val firstRequest = requestPut(url, jsonFileContentFirst)
            val firstResponseFuture: Future[HttpResponse] = Http().singleRequest(firstRequest)
            val firstBodyStringFuture: Future[String] = firstResponseFuture.flatMap { response =>
                response.entity.toStrict(5.seconds).map(_.data.utf8String)
            }

            val returnedJsonGameAndField = Await.result(firstBodyStringFuture, 5.seconds)
            val (newGame, newField): (GameDTO, FieldDTO) = RestUtil.jsonToGameAndFieldDTO(returnedJsonGameAndField)
            field = newField
            this.game = newGame
            
            val jasonField2 = RestUtil.fieldDtoToJson(field)
            val jsonFileContent2 = jasonField2.getBytes("UTF-8")

            val request2 = HttpRequest(
                method =  HttpMethods.PUT,
                uri = s"http://model:9082/model/field/showInvisibleCell?y=${move.y}&x=${move.x}",
                entity = HttpEntity(ContentTypes.`application/json`, jsonFileContent2)
            )

            val responseFuture: Future[HttpResponse] = Http().singleRequest(request2)
            val bodyStringFuture: Future[String] = responseFuture.flatMap { response =>
                response.entity.toStrict(5.seconds).map(_.data.utf8String)
            }

            val result = Await.result(bodyStringFuture, 5.seconds)
            val symbol = if (result.length()>3) {"E"} else {result}

            if(symbol == "E"){field}
            else if(symbol == "0"){field}
            else{undoRedoManager.doStep(field, DoCommand(move))}
        }

    override def loadGame: Unit = {
        def loadGameDTO(): Unit = {
            val gameUri = Uri("http://persistence:9083/persistence/game")
            val gameRequestSource = akka.stream.scaladsl.Source.single(HttpRequest(method = HttpMethods.GET, uri = gameUri))
            val requestFlow = Flow[HttpRequest].mapAsync(1)(Http().singleRequest(_))
            val responseFlow = Flow[HttpResponse].mapAsync(1)(response => Unmarshal(response.entity).to[String])

            val gameSink = Sink.foreach[String] { jsonString =>
                Try(RestUtil.jsonToGameDTO(jsonString)) match {
                    case Success(gameDto) =>
                        this.game = gameDto
                        println("game loaded from rest")
                        notifyObserversRest("Load")
                    case Failure(_) =>
                }
            }
            val gameGraph = GraphDSL.create() { implicit builder =>
                import GraphDSL.Implicits._

                val requestFlowShape = builder.add(requestFlow)
                val responseFlowShape = builder.add(responseFlow)

                gameRequestSource ~> requestFlowShape ~> responseFlowShape ~> gameSink

                ClosedShape
            }
            RunnableGraph.fromGraph(gameGraph).run()
        }
        def loadFieldDTO(): Unit = {
            val fieldUri = Uri("http://persistence:9083/persistence/field")
            val fieldRequestSource = akka.stream.scaladsl.Source.single(HttpRequest(method = HttpMethods.GET, uri = fieldUri))
            val requestFlow = Flow[HttpRequest].mapAsync(1)(Http().singleRequest(_))
            val responseFlow = Flow[HttpResponse].mapAsync(1)(response => Unmarshal(response.entity).to[String])

            val fieldSink = Sink.foreach[String] { jsonString =>
                Try(RestUtil.jsontToFieldDTO(jsonString)) match {
                    case Success(fieldDto) =>
                        this.field = fieldDto
                        println("field loaded")
                        notifyObserversRest("Load")
                    case Failure(_) =>
                }
            }
            val fieldGraph = GraphDSL.create() { implicit builder =>
                import GraphDSL.Implicits._

                val requestFlowShape = builder.add(requestFlow)
                val responseFlowShape = builder.add(responseFlow)

                fieldRequestSource ~> requestFlowShape ~> responseFlowShape ~> fieldSink

                ClosedShape
            }
            RunnableGraph.fromGraph(fieldGraph).run()
        }
        loadGameDTO()
        loadFieldDTO()
    }

    def saveGame =

        notifyObserversRest("SaveTime")

        val queryParameters = Uri.Query(
            "bombs" -> s"${game.bombs}",
            "size"  -> s"${game.side}",
            "time"  -> s"${game.time}",
            "board" -> s"${game.board}"
        )

        val uri = Uri("http://persistence:9083/persistence/putGame").withQuery(queryParameters)
        val request = HttpRequest(method = HttpMethods.PUT, uri = uri)

        val responseFuture: Future[HttpResponse] = Http().singleRequest(request)

        responseFuture.onComplete {
            case Success(res) => println(s"Request successful: $res")
            case Failure(ex)  => println(s"Request failed: $ex")
        }

        val jasonField = RestUtil.fieldDtoToJson(field)
        val jsonFileContent = jasonField.getBytes("UTF-8")

        val request2 = HttpRequest(
            method =  HttpMethods.PUT,
            uri = "http://persistence:9083/persistence/putField",
            entity = HttpEntity(ContentTypes.`application/json`, jsonFileContent)
        )

        val responseFuture2: Future[HttpResponse] = Http().singleRequest(request2)
        responseFuture2.onComplete {
            case Success(res) => println(s"Successfully saved JSON file field: ${res.entity}")
            case Failure(ex)  => println(s"Failed to put JSON file: ${ex.getMessage}")
        }

        game = GameDTO(game.bombs, game.side, game.time, "Playing") // T-D-G-T
        notifyObserversRest("Save")
    
    def exit = notifyObserversRest("Exit")

    def gameOver: Unit =
        val url = s"http://model:9082/model/field/gameOverField"
        val jsonField =  RestUtil.fieldDtoToJson(field)
        val jsonFileContent = jsonField.getBytes("UTF-8")
        
        val request = HttpRequest(
            method =  HttpMethods.PUT,
            uri = url,
            entity = HttpEntity(ContentTypes.`application/json`, jsonFileContent)
        )
        val responseFuture: Future[HttpResponse] = Http().singleRequest(request)
        val bodyStringFuture: Future[String] = responseFuture.flatMap { response =>
            response.entity.toStrict(5.seconds).map(_.data.utf8String)
        }
        val bodyString = Await.result(bodyStringFuture, 5.seconds)
        
        val tempField = RestUtil.jsontToFieldDTO(bodyString)
        field = FieldDTO(tempField.hidden, tempField.hidden)
        notifyObserversRest("GameOver") 
    
    def openRec(x: Int, y: Int, field: FieldDTO): FieldDTO = undoRedoManager.doStep(field, DoCommand(Move("recursion", x, y)))

    def helpMenu = 
        helpMenuRest
        notifyObserversRest("Help")
    
    def fieldToString: String = {
        val url2 = s"http://model:9082/model/field/toString"
        val bodyField = RestUtil.fieldDtoToJson(field)

        val request2 = HttpRequest(
            method =  HttpMethods.PUT,
            uri = url2,
            entity = HttpEntity(ContentTypes.`application/json`, bodyField)
        )

        val responseFuture2: Future[HttpResponse] = Http().singleRequest(request2) 
        val bodyStringFuture2: Future[String] = responseFuture2.flatMap { response =>
            response.entity.toStrict(5.seconds).map(_.data.utf8String)
        }
        val bodyString2 = Await.result(bodyStringFuture2, 5.seconds)
        bodyString2
    }

    def helpMenuRest: String = 
        val url = s"http://model:9082/model/game/helpMessage"
        
        val request = HttpRequest(
            method =  HttpMethods.GET,
            uri = url
        )

        val responseFuture: Future[HttpResponse] = Http().singleRequest(request)
        val bodyStringFuture: Future[String] = responseFuture.flatMap { response =>
            response.entity.toStrict(5.seconds).map(_.data.utf8String)
        }
        val bodyStringHelpMessage = Await.result(bodyStringFuture, 5.seconds)
        bodyStringHelpMessage
    
    def cheatRest: String = {
        val url = s"http://model:9082/model/field/cheat"
        
        val bodyField = RestUtil.fieldDtoToJson(field)

        val request = HttpRequest(
            method =  HttpMethods.PUT,
            uri = url,
            entity = HttpEntity(ContentTypes.`application/json`, bodyField)
        )

        val responseFuture: Future[HttpResponse] = Http().singleRequest(request)
        val bodyStringFuture: Future[String] = responseFuture.flatMap { response =>
            response.entity.toStrict(5.seconds).map(_.data.utf8String)
        }
        val bodyString = Await.result(bodyStringFuture, 5.seconds)
        bodyString
    }

    def checkGameOver(status: String) = {
        val url = s"http://model:9082/model/game/checkExit?board=$status"
        val request = HttpRequest(
            method =  HttpMethods.GET,
            uri = url
        )
        val responseFuture: Future[HttpResponse] = Http().singleRequest(request)
        val bodyStringFuture: Future[String] = responseFuture.flatMap { response =>
            response.entity.toStrict(5.seconds).map(_.data.utf8String)
        }
        val bodyString = Await.result(bodyStringFuture, 5.seconds)
        bodyString.toBoolean
    }

    def checkGameOverGui: Boolean = {
        val status = game.board

        val url = s"http://model:9082/model/game/checkExit?board=$status"
        val request = HttpRequest(
            method = HttpMethods.GET,
            uri = url
        )
        val responseFuture: Future[HttpResponse] = Http().singleRequest(request)
        val bodyStringFuture: Future[String] = responseFuture.flatMap { response =>
            response.entity.toStrict(5.seconds).map(_.data.utf8String)
        }
        val bodyString = Await.result(bodyStringFuture, 5.seconds)
        bodyString.toBoolean
    }

    def newGameGUI =
        game = GameDTO(game.bombs, game.side, game.time, "Playing")
        notifyObserversRest("Input")


    def newGameField(optionString: Option[String]) =
        game = GameDTO(game.bombs, game.side, game.time, "Playing")

        val oString = optionString.get match {
            case "SuperEasy" => 0
            case "Easy" => 1 
            case "Medium" => 2
            case "Hard" => 3
        }
        
        val url = s"http://model:9082/model/game/newGameField?optionString=$oString"
        val bodyGame = RestUtil.gameDtoToJson(game)
        val body = bodyGame.getBytes("UTF-8")

        val request = requestPut(url, body)
        val responseFuture: Future[HttpResponse] = Http().singleRequest(request)
        val bodyStringFuture: Future[String] = responseFuture.flatMap { response =>
            response.entity.toStrict(5.seconds).map(_.data.utf8String)
        }
        val returnedJsonGameAndField = Await.result(bodyStringFuture, 5.seconds)
        val (newGame, newField): (GameDTO, FieldDTO) = RestUtil.jsonToGameAndFieldDTO(returnedJsonGameAndField)
        field = newField                
        game = GameDTO(newGame.bombs, newGame.side, newGame.time, newGame.board) 
        notifyObserversRest("NewGame")
    
    def newGame(side: Int, bombs: Int) =
        game = GameDTO(bombs, side, game.time, "Playing")
        field = createFieldDTO(game)
        notifyObserversRest("NewGame")
        
    def newGameForGui(side: Int, bombs: Int): Unit = 
        game = GameDTO(bombs, side, game.time, "Playing")
        field = createFieldDTO(game)
        notifyObserverGui("NewGame")
        
    def notifyObserverGui(event: String) = {
        val GuiCommandTopic = "gui-notify"
        GuiNotificationProducer(system).sendCommand(event, GuiCommandTopic)
    }

    def makeAndPublish(makeThis: (Boolean, Move, GameDTO) => FieldDTO, b: Boolean, move: Move, game: GameDTO): Unit =
        field = makeThis(b, move, game)
        
        val jasonField = RestUtil.fieldDtoToJson(field)
        val jsonFileContent = jasonField.getBytes("UTF-8")
        val request = requestPut(s"http://model:9082/model/field/showInvisibleCell?y=${move.y}&x=${move.x}", jsonFileContent)
        val responseFuture: Future[HttpResponse] = Http().singleRequest(request)
        val bodyStringFuture: Future[String] = responseFuture.flatMap { response =>
            response.entity.toStrict(5.seconds).map(_.data.utf8String)
        }

        val result = Await.result(bodyStringFuture, 5.seconds)
        val symbol = if(result.length > 3){"E"} else {result}

        if (symbol == "0"){field = openRec(move.x,move.y,field)}
        val firstOrNext = if (b) "Start" else "Next"
        notifyObserversRest(firstOrNext)
    
    def makeAndPublish(makeThis: Move => FieldDTO, move: Move): Unit =
        move.value match {
            case "flag" => 
                val jasonField = RestUtil.fieldDtoToJson(field)
                val jsonFileContent = jasonField.getBytes("UTF-8")

                val request = HttpRequest(
                    method =  HttpMethods.PUT,
                    uri = s"http://model:9082/model/field/put/flag?x=${move.x}&y=${move.y}",
                    entity = HttpEntity(ContentTypes.`application/json`, jsonFileContent)
                )

                val responseFuture: Future[HttpResponse] = Http().singleRequest(request)
                val bodyFieldFuture: Future[String] = responseFuture.flatMap { response =>
                    response.entity.toStrict(5.seconds).map(_.data.utf8String)
                }

                var jsonBodyField = "E"
                bodyFieldFuture.onComplete {
                    case Success(bodyField) =>
                        jsonBodyField = bodyField
                    case Failure(ex) =>
                        sys.error(s"something wrong: ${ex.getMessage}")
                }

                val result = Await.result(bodyFieldFuture, 5.seconds)
                jsonBodyField = result

                val fieldFromController = RestUtil.jsontToFieldDTO(jsonBodyField)
                field = fieldFromController

            case "unflag" => 
                val jasonField = RestUtil.fieldDtoToJson(field)
                val jsonFileContent = jasonField.getBytes("UTF-8")

                val request2 = HttpRequest(
                    method =  HttpMethods.PUT,
                    uri = s"http://model:9082/model/field/put/removeFlag?x=${move.x}&y=${move.y}",
                    entity = HttpEntity(ContentTypes.`application/json`, jsonFileContent)
                )

                val responseFuture: Future[HttpResponse] = Http().singleRequest(request2)
                val bodyFieldFuture: Future[String] = responseFuture.flatMap { response =>
                    response.entity.toStrict(5.seconds).map(_.data.utf8String)
                }

                var jsonBodyField = "E"
                bodyFieldFuture.onComplete {
                    case Success(bodyField) =>
                        jsonBodyField = bodyField
                    case Failure(ex) =>
                        sys.error(s"something wrong: ${ex.getMessage}")
                }

                val result = Await.result(bodyFieldFuture, 5.seconds)
                jsonBodyField = result

                val fieldFromController = RestUtil.jsontToFieldDTO(jsonBodyField)
                field = fieldFromController
        }

        field = makeThis(move)
        notifyObserversRest("Next")
    
    def makeAndPublish(makeThis: => FieldDTO): FieldDTO =
        field = makeThis
        notifyObserversRest("Next")
        field

    def saveScoreAndPlayerName(playerName: String, saveScore: Int, filePath: String) = {

        val queryParameters = Uri.Query(
            "player" -> s"${playerName}",
            "score"  -> s"${saveScore}"
        )

        val uri = Uri("http://persistence:9083/persistence/putHighscore").withQuery(queryParameters)
        val request = HttpRequest(method = HttpMethods.PUT, uri = uri)

        val responseFuture: Future[HttpResponse] = Http().singleRequest(request)

        responseFuture.onComplete {
        case Success(res) => println(s"Request successful: $res")
        case Failure(ex)  => println(s"Request failed: $ex")
        }
    }

    def loadPlayerScores: Seq[(String, Int)] = {
        
        val url = "http://persistence:9083/persistence/highscore"
        val request = HttpRequest(method = HttpMethods.GET, uri = url)
        val responseFuture: Future[HttpResponse] = Http().singleRequest(request)
        val bodyFuture: Future[String] = responseFuture.flatMap { response =>
            response.entity.toStrict(5.seconds).map(_.data.utf8String)
        }
        val jsonStringApiResult: String = Await.result(bodyFuture, 5.seconds)
        
        case class PlayerScore(player: String, score: Int)
        implicit val playerScoreFormat: Format[PlayerScore] = Json.format[PlayerScore]
        
        val json: JsValue = Json.parse(jsonStringApiResult)
        val playerScoresResult: JsResult[Seq[PlayerScore]] = Json.fromJson[Seq[PlayerScore]](json)

        val playerScores: Seq[(String, Int)] = playerScoresResult match {
        case JsSuccess(scores, _) => scores.map(ps => (ps.player, ps.score))
        case JsError(errors) => Seq.empty
        }

        playerScores
    }
    
    def put(move: Move): FieldDTO = undoRedoManager.doStep(field, DoCommand(move))
    def undo: FieldDTO = undoRedoManager.undoStep(field)
    def redo: FieldDTO = undoRedoManager.redoStep(field)
    def saveTime(currentTime: Int): Unit = 
        val tempGame: GameDTO = GameDTO(game.bombs, game.side, currentTime, "Playing")
        game = tempGame
    
    def requestPut(input_uri: String, jsonContent: Array[Byte]) = {
        HttpRequest(
            method =  HttpMethods.PUT,
            uri = input_uri,
            entity = HttpEntity(ContentTypes.`application/json`, jsonContent)
        )
    }

    def notifyObserversRest(event: String): Unit = {

        val TuiCommandTopic = "tui-notify"
        TuiNotificationProducer(system).sendCommand(event, TuiCommandTopic)
        
        val GuiCommandTopic = "gui-notify"
        GuiNotificationProducer(system).sendCommand(event, GuiCommandTopic)
        
    }
