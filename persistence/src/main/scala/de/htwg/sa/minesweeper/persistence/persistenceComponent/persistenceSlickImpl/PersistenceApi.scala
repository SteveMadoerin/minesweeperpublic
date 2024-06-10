package de.htwg.sa.minesweeper.persistence.persistenceComponent.persistenceSlickImpl

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpRequest, HttpResponse, ResponseEntity, StatusCodes}
import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.unmarshalling.{FromEntityUnmarshaller, Unmarshaller}
import akka.stream.Materializer
import de.htwg.sa.minesweeper.persistence.entity.*
import de.htwg.sa.minesweeper.persistence.persistenceComponent.IPersistence
import de.htwg.sa.minesweeper.persistence.persistenceComponent.config.Default
import play.api.libs.json.*
import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.scaladsl.GraphDSL.Builder
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, Merge, Sink, Source}
import akka.stream.{FlowShape, UniformFanInShape, UniformFanOutShape}

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import java.io.File
import java.nio.file.Paths
import scala.concurrent.ExecutionContextExecutor
import scala.io.Source
import scala.util.{Failure, Success, Try}

class PersistenceApi(using var p: IPersistence) {
    
    implicit val jsValueUnmarshaller: FromEntityUnmarshaller[JsValue] = {
        Unmarshaller.byteStringUnmarshaller.forContentTypes(ContentTypes.`application/json`).mapWithCharset { (data, charset) =>
            Json.parse(data.decodeString(charset.nioCharset.name))
        }
    }
    
    //var db = new Persistence()
    implicit val system: ActorSystem = ActorSystem("PersistenceService")
    implicit val materializer: Materializer = Materializer(system)
    implicit val executionContext: ExecutionContextExecutor = system.dispatcher

    private val pathToFile = Default.filePathHighScore

    private val persistFlow: Flow[HttpRequest, String, NotUsed] = Flow.fromGraph(GraphDSL.create() { implicit builder: Builder[NotUsed] =>
        import GraphDSL.Implicits._

        val broadcast: UniformFanOutShape[HttpRequest, HttpRequest] = builder.add(Broadcast[HttpRequest](2))
        val merge: UniformFanInShape[String, String] = builder.add(Merge[String](2))

        val leGetRequestFlow = Flow[HttpRequest].map { req =>
            req.uri.path.toString match {
                case "/persistence/ping" =>
                    HttpResponse(entity = "accessGranted")
                case "/persistence/game" =>
                    val spiel = p.loadGame
                    HttpResponse(entity = spiel.get.gameToJson.toString())
                case "/persistence/field" =>
                    val field = p.loadField
                    HttpResponse(entity = field.get.fieldToJson.toString())
                case "/persistence/highscore" =>
                    val scoreTable = p.loadPlayerScores(pathToFile)
                    val highscore = loadPlayerScoresToJson(scoreTable)
                    HttpResponse(entity = highscore.toString())
                case _ =>
                    println(req.uri.path.toString)
                    HttpResponse(status = StatusCodes.NotFound)
            }
        }

        val leGetRequestFlowShape = builder.add(leGetRequestFlow)

        val leGetRequestResponseFlow = Flow[HttpResponse].mapAsync(1) { resp =>
            Unmarshal(resp.entity).to[String]
        }

        val leGetResponseFlowShape = builder.add(leGetRequestResponseFlow)


        val putRequestFlow: Flow[HttpRequest, HttpResponse, NotUsed] = Flow[HttpRequest].mapAsync(3) { req =>
            req.uri.path.toString match {
                case "/persistence/putField" => {
                    req.entity.toStrict(Duration.apply(3, TimeUnit.SECONDS)).map { entity =>
                        val requestBody = entity.data.utf8String
                        val maybeSucces = Try(p.saveField(Util.f.jsonToField(requestBody)))
                        // ContentTypes.`application/json`
                        HttpResponse(entity = requestBody)
                    }
                }

                case "/persistence/putGame" => {
                    req.entity.toStrict(Duration.apply(3, TimeUnit.SECONDS)).flatMap { entity =>
                        ((req.uri.query().get("bombs"), req.uri.query().get("size"), req.uri.query().get("time"), req.uri.query().get("board"))) match {
                            case (Some(bombs), Some(size), Some(time), Some(board)) => {
                                val tempGame = Game(bombs.toInt, size.toInt, time.toInt, board.toString)
                                Try(p.saveGame(tempGame)) match {
                                    case Success(_) =>
                                        Future.successful(HttpResponse(entity = tempGame.gameToJson))
                                    case Failure(_) =>
                                        Future.successful(HttpResponse(status = StatusCodes.BadRequest, entity = "Error saving game."))
                                }
                            }
                            case _ =>
                                Future.successful(HttpResponse(status = StatusCodes.BadRequest, entity = "Invalid query parameters."))
                        }
                    }
                }
                
                // http://localhost:9083/persistence/putHighscore?player=Sly&score=777
                case "/persistence/putHighscore" => {
                    req.entity.toStrict(Duration.apply(3, TimeUnit.SECONDS)).map { entity =>
                        val player = (req.uri.query().get("player")).get
                        val score = (req.uri.query().get("score")).get.toInt
                        val newScoreObj = Json.obj(
                            "player" -> player,
                            "score" -> score
                        )
                        val path = pathToFile
                        p.savePlayerScore(player, score, pathToFile)
                        val requestBody = entity.data.utf8String
                        HttpResponse(entity = requestBody + "\n" + newScoreObj.toString())
                    }
                }

                case _ => {
                    println(req.uri.path.toString)
                    Future.successful(HttpResponse(404, entity = req.uri.path.toString))
                }
            }
        }


        val postResponseFlow: Flow[HttpResponse, String, NotUsed] = Flow[HttpResponse].mapAsync(1) { resp =>
            Unmarshal(resp.entity).to[String]
        }


        val postResponseFlowShape = builder.add(postResponseFlow)
        val putRequestFlowShape = builder.add(putRequestFlow)

        broadcast.out(0) ~> leGetRequestFlowShape ~> leGetResponseFlowShape ~> merge.in(0)
        broadcast.out(1) ~> putRequestFlowShape ~> postResponseFlowShape ~> merge.in(1)


        FlowShape(broadcast.in, merge.out)

    })

    //val bindFuture = Http().newServerAt("0.0.0.0", 9083).bind(route)
    Http().newServerAt("localhost", 9083).bind(
        pathPrefix("persistence") {
            extractRequest { request =>
                complete(
                    akka.stream.scaladsl.Source.single(request).via(persistFlow).runWith(Sink.head).map(resp => resp)
                )
            }
        }
    )

    def start(): Future[Nothing] = Future.never

    def loadPlayerScoresToJson(filePath: String): JsValue = {

        val file = new File(filePath)
        val source = io.Source.fromFile(file)
        val scoresJson = Try(Json.parse(source.mkString)).getOrElse(JsArray())
        source.close()

        scoresJson match {
            case JsArray(scores) =>
                val validScores = scores.flatMap {
                    case scoreJson if (scoreJson \ "player").validate[String].isSuccess &&
                        (scoreJson \ "score").validate[Int].isSuccess =>
                        Some(scoreJson)
                    case _ => None
                }
                JsArray(validScores)
            case _ => JsArray()
        }
    }

    def loadPlayerScoresToJson( scoreTable: Seq[(String, Int)]): JsValue = {

        val validScores = scoreTable.flatMap {
            case (player, score) if player.nonEmpty && score >= 0 =>
                Some(Json.obj("player" -> player, "score" -> score))
            case _ => None
        }
        JsArray(validScores)
    }

}
