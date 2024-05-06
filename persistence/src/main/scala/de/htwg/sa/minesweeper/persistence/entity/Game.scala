package de.htwg.sa.minesweeper.persistence.entity

import scala.io.StdIn.readLine
import scala.util.{Random, Try}
import scala.annotation.tailrec
import play.api.libs.json.Json
import play.api.libs.json.JsValue

case class Game (bombs : Int, side: Int, time: Int, board : String) extends IGame:
    

    def gameToJson: String = {
        Json.prettyPrint(
            Json.obj(
                "game" -> Json.obj(
                    "status" -> this.board,
                    "bombs" -> this.bombs,
                    "side" -> this.side,
                    "time" -> this.time
                )
            )
        )
    }

    def jsonToGame(jsonString: String): IGame = {
        val json: JsValue = Json.parse(jsonString)
        val status = (json \ "game" \ "status").get.toString
        val bombs = (json \ "game" \ "bombs").get.toString.toInt
        val side = (json \ "game" \ "side").get.toString.toInt
        val time = (json \ "game" \ "time").get.toString.toInt
        val statusWithoutQuotes = status.replace("\"", "") // \Playing\ -> Playing
        Game(bombs, side, time, statusWithoutQuotes)
    }


end Game

