package de.htwg.sa.minesweeper.persistence.persistenceComponent.persistenceSlickImpl

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.unmarshalling.{FromEntityUnmarshaller, Unmarshaller}
import akka.stream.Materializer
import de.htwg.sa.minesweeper.persistence.entity.*
import de.htwg.sa.minesweeper.persistence.persistenceComponent.IPersistence
import de.htwg.sa.minesweeper.persistence.persistenceComponent.config.Default
import play.api.libs.json.*

import java.io.File
import java.nio.file.Paths
import scala.concurrent.ExecutionContextExecutor
import scala.io.Source
import scala.util.{Failure, Success, Try}

class PersistenceApiBackup(using var p: IPersistence) {
    
    implicit val jsValueUnmarshaller: FromEntityUnmarshaller[JsValue] = {
        Unmarshaller.byteStringUnmarshaller.forContentTypes(ContentTypes.`application/json`).mapWithCharset { (data, charset) =>
            Json.parse(data.decodeString(charset.nioCharset.name))
        }
    }
    
    var db = new Persistence()
    implicit val system: ActorSystem = ActorSystem()
    implicit val materializer: Materializer = Materializer(system)
    implicit val executionContext: ExecutionContextExecutor = system.dispatcher

    private val pathToFile = Default.filePathHighScore
    
    val route: Route = pathPrefix("persistence") {
        get {
            path("ping") {
                complete(StatusCodes.OK, HttpEntity(ContentTypes.`application/json`, "accessGranted"))
            } ~
            path("game") {
                val spiel = p.loadGame
                complete(StatusCodes.OK, HttpEntity(ContentTypes.`application/json`, spiel.get.gameToJson.toString())    )
            } ~
            path("field") {
                val field = p.loadField.getOrElse(Util.f)
                complete(HttpEntity(ContentTypes.`application/json`, field.fieldToJson.toString))
            } ~
            path("highscore") {

                val scoreTable = p.loadPlayerScores(pathToFile)
                val highscore = loadPlayerScoresToJson(scoreTable)

                complete(HttpEntity(ContentTypes.`application/json`, highscore.toString))
            }
        } ~
        put {
            path("putGame") {
                parameter("bombs".as[Int], "size".as[Int], "time".as[Int], "board".as[String]) {
                    case (bombs, size, time, board) =>
                        val tempGame = Game(bombs, size, time, board)
                        p.saveGame(tempGame)
                        complete(HttpEntity(ContentTypes.`application/json`, tempGame.gameToJson))
                }
            } ~
            path("putField") {
                entity(as[String]) { field =>
                    val jsonField = field
                    val pathToFile = Paths.get("C:\\Playground\\minesweeperpublic\\src\\main\\data\\field.json")
                    
                    val maybeSucces = Try(p.saveField(Util.f.jsonToField(field)))

                    maybeSucces match
                        case Success(_) => complete(HttpEntity(ContentTypes.`application/json`, jsonField))
                        case Failure(_) => complete(StatusCodes.InternalServerError, "An error occurred")
                    
                    complete(HttpEntity(ContentTypes.`application/json`, jsonField))
                    
                }
            } ~
            path("putHighscore") {
                parameter("player".as[String], "score".as[Int]) { (player, score) =>

                    val newScoreObj = Json.obj(
                        "player" -> player,
                        "score" -> score
                    )
                    val path = pathToFile
                    p.savePlayerScore(player, score, pathToFile)

                    complete(HttpEntity(ContentTypes.`application/json`, newScoreObj.toString()))

                }
            }
        }
    }

    def start(): Unit = {
        val bindFuture = Http().newServerAt("0.0.0.0", 9083).bind(route)

        bindFuture.onComplete {
            case Success(binding) =>
                println("Server online at http://localhost:9083/")
                complete(binding.toString)
            case Failure(exception) =>
                println(s"An error occurred: $exception")
                complete("fail binding")
        }
    }

    def loadPlayerScoresToJson(filePath: String): JsValue = {

        val file = new File(filePath)
        val source = Source.fromFile(file)
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
