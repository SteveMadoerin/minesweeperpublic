package de.htwg.sa.minesweeper.persistence.persistenceComponent

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.unmarshalling.{FromEntityUnmarshaller, Unmarshaller}
import akka.stream.Materializer
import de.htwg.sa.minesweeper.persistence.entity.*
import de.htwg.sa.minesweeper.persistence.persistenceComponent.config.Default
import de.htwg.sa.minesweeper.persistence.persistenceComponent.persistenceSlickImpl.Util
import play.api.libs.json.*

import java.io.File
import java.nio.file.{Files, Paths}
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.io.Source
import scala.util.{Failure, Success, Try}

class PersistenceApi(using var file: IPersistence) {
    
    implicit val jsValueUnmarshaller: FromEntityUnmarshaller[JsValue] = {
        Unmarshaller.byteStringUnmarshaller.forContentTypes(ContentTypes.`application/json`).mapWithCharset { (data, charset) =>
            Json.parse(data.decodeString(charset.nioCharset.name))
        }
    }
    
    var db = new Slick()
    implicit val system: ActorSystem = ActorSystem()
    implicit val materializer: Materializer = Materializer(system)
    implicit val executionContext: ExecutionContextExecutor = system.dispatcher

    val pathToFile = Default.filePathHighScore
    
    val route: Route = pathPrefix("persistence") {
        get {
            path("game") {
                val game = Slick().loadGame
                Slick().closeConnection
            complete(StatusCodes.OK, HttpEntity(ContentTypes.`application/json`, game.get.gameToJson.toString())    )
            } ~
            path("field") {
                val field = Slick().loadField.getOrElse(Util.f)
                Slick().closeConnection
                complete(HttpEntity(ContentTypes.`application/json`, field.fieldToJson.toString))
            } ~
            path("highscore") {

                Slick().loadPlayerScores(pathToFile)
                Slick().closeConnection
                complete(HttpEntity(ContentTypes.`application/json`, loadPlayerScoresToJson("C:\\Playground\\minesweeperpublic\\src\\main\\data\\highscore.json").toString()))
            }
        } ~
        put {
            path("putGame") {
                parameter("bombs".as[Int], "size".as[Int], "time".as[Int], "board".as[String]) { (bombs, size, time, board) =>
            
                    val tempGame = Game(bombs, size, time, board)
                    file.saveGame(tempGame)
                    Slick().saveGame(tempGame)
                    Slick().closeConnection
                    complete(HttpEntity(ContentTypes.`application/json`, tempGame.gameToJson.toString()))
                }
            } ~
            path("putField") {
                entity(as[String]) { field =>
                    val jsonField = field
                    val pathToFile = Paths.get("C:\\Playground\\minesweeperpublic\\src\\main\\data\\field.json")
                    
                    Slick().saveField(Util.f.jsonToField(field))
                    Slick().closeConnection

                    val saveFuture: Future[Unit] = Future {
                        Files.write(pathToFile, jsonField.getBytes("UTF-8"))
                    }

                    onComplete(saveFuture) {
                        case Success(_) => complete(StatusCodes.OK, HttpEntity(ContentTypes.`application/json`, jsonField))
                        case Failure(ex) => complete(StatusCodes.InternalServerError, s"An error occurred: ${ex.getMessage}")
                    }
                }
            } ~
            path("putHighscore") {
                parameter("player".as[String], "score".as[Int]) { (player, score) =>

                    val newScoreObj = Json.obj(
                        "player" -> player,
                        "score" -> score
                    )
                    Slick().savePlayerScore(player, score, "")
                    Slick().closeConnection
                    
                    val pathToFile = "C:\\Playground\\minesweeperpublic\\src\\main\\data\\highscore.json"

                    file.savePlayerScore(player, score, pathToFile)
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

/*    val bindFuture = Http().bindAndHandle(route, "localhost", 9083)

    def unbind = bindFuture
        .flatMap(_.unbind())
        .onComplete(_ => system.terminate())*/

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
    

}
