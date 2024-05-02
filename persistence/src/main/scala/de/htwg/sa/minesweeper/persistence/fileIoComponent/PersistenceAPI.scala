package de.htwg.sa.minesweeper.persistence.fileIoComponent


import scala.concurrent.ExecutionContextExecutor
import scala.util.Failure
import scala.util.Success
import scala.util.matching.Regex
import scala.util.Try

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, Materializer}
import scala.concurrent.ExecutionContext
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.RouteDirectives
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Route, StandardRoute}
import de.htwg.sa.minesweeper.model.gameComponent.IGame
import de.htwg.sa.minesweeper.model.gameComponent.IField


import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.unmarshalling.FromRequestUnmarshaller
import akka.http.scaladsl.model.HttpEntity
import akka.util.ByteString
import play.api.libs.json._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import akka.http.scaladsl.unmarshalling.{FromEntityUnmarshaller, Unmarshaller}
import akka.http.scaladsl.model.ContentTypes
import de.htwg.sa.minesweeper.model.gameComponent.gameBaseImpl.Game
import de.htwg.sa.minesweeper.persistence.fileIoComponent.config.Default
import de.htwg.sa.minesweeper.model.gameComponent.gameBaseImpl.Field
import scala.io.Source
import java.nio.file.Files
import java.nio.file.Paths
import akka.http.scaladsl.model.StatusCodes
import java.io.File

class PersistenceApi(using var file: IFileIO) {

    // Define the implicit unmarshaller for `play.api.libs.json.JsValue`
    implicit val jsValueUnmarshaller: FromEntityUnmarshaller[JsValue] = {
        Unmarshaller.byteStringUnmarshaller.forContentTypes(ContentTypes.`application/json`).mapWithCharset { (data, charset) =>
            Json.parse(data.decodeString(charset.nioCharset.name))
        }
    }


    implicit val system: ActorSystem = ActorSystem()
    implicit val materializer: Materializer = Materializer(system)
    implicit val executionContext: ExecutionContextExecutor = system.dispatcher

    val pathToFile = "highscore.json" // TODO: change path to highscore.json

    // we net also a put method to save the game
    
    val route: Route = pathPrefix("persistence") {
        get {
            path("game") {
            complete(HttpEntity(ContentTypes.`application/json`, file.loadGame.get.gameToJson.toString()))
            } ~
            path("field") {
            complete(HttpEntity(ContentTypes.`application/json`, file.loadField.get.fieldToJson))
            } ~
            path("highscore") {
            complete(HttpEntity(ContentTypes.`application/json`, loadPlayerScoresToJson("C:\\Playground\\minesweeperpublic\\src\\main\\data\\highscore.json").toString()))
            }
        } ~
        put {
            path("putGame") {
                parameter("bombs".as[Int], "size".as[Int], "time".as[Int], "board".as[String]) { (bombs, size, time, board) =>
            
                    val testGame: IGame = Game(bombs, size, time, board)
                    file.saveGame(testGame)
                    complete(HttpEntity(ContentTypes.`application/json`, Game(bombs, size, time, board).gameToJson.toString()))
                }
            } ~
            path("putField") {
                entity(as[String]) { field =>
                    val jsonField = field
                    val pathToFile = Paths.get("C:\\Playground\\minesweeperpublic\\src\\main\\data\\field.json")

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

                    val pathToFile = "C:\\Playground\\minesweeperpublic\\src\\main\\data\\highscore.json"

                    file.savePlayerScore(player, score, pathToFile)
                    complete(HttpEntity(ContentTypes.`application/json`, newScoreObj.toString())) 

                }
            }
        }
    }

    /*      val request = Post("/users").withEntity(userEntity)
         GET     /user/{id}            Application.user
        POST    /user/                Application.createUser
        PUT     /user/{id}            Application.updateUser
        DELETE  /user/{id}            Application.deleteUser */

    val bindFuture = Http().bindAndHandle(route, "localhost", 8083)

    def unbind = bindFuture
        .flatMap(_.unbind())
        .onComplete(_ => system.terminate())

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
