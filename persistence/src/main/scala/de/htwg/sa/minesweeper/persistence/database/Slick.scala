package de.htwg.sa.minesweeper.persistence.database

import akka.japi.JAPI
import de.htwg.sa.minesweeper.persistence.entity.Game
import play.api.libs.json.Json
import slick.jdbc.PostgresProfile.api.*
import slick.jdbc.JdbcBackend.Database
import slick.lifted.TableQuery
import slick.dbio.DBIO

import java.sql.SQLNonTransientException
import scala.concurrent.Await
import scala.util.{Failure, Success}
import concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt


class Slick extends IDAO {

  private val databaseDB: String = sys.env.getOrElse("POSTGRES_DATABASE", "postgres")
  private val databaseUser: String = sys.env.getOrElse("POSTGRES_USER", "postgres")
  private val databasePassword: String = sys.env.getOrElse("POSTGRES_PASSWORD", "postgres")
  private val databasePort: String = sys.env.getOrElse("POSTGRES_PORT", "5432")
  private val databaseHost: String = sys.env.getOrElse("POSTGRES_HOST", "localhost")
  private val databaseUrl = s"jdbc:postgresql://$databaseHost:$databasePort/$databaseDB?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&autoReconnect=true"

  val database = Database.forURL(
    url = databaseUrl,
    driver = "org.postgresql.Driver",
    user = databaseUser,
    password = databasePassword
  )


  private val gameTable = TableQuery(GameTable(_))
  private val fieldTable = TableQuery(FieldTable(_))
  private val playerTable = TableQuery(PlayerScoreTable(_))

  private val setup: DBIOAction[Unit, NoStream, Effect.Schema] = DBIO.seq(
    gameTable.schema.createIfNotExists,
    fieldTable.schema.createIfNotExists,
    playerTable.schema.createIfNotExists
  )
  println("create tables")

  database.run(setup).onComplete {
    case Success(value) => println("tables created")
    case Failure(exception) => println(exception.getMessage)
  }

  def savePlayerScore(playerName: String, score: Int): Unit = {

    val playerscore = (playerTable returning playerTable.map(_.id)) += (
      None,
      score,
      playerName
    )
    database.run(playerscore)
    println("Database save")
  }

  def saveField(field: String): Unit = {
    val fields = (fieldTable returning fieldTable.map(_.id)) += (
      None,
      field
    )
    database.run(fields)
    println("Database save")
  }

  def saveGame(bombs: Int, size: Int, time: Int, board: String): Unit = {
    val games = (gameTable returning gameTable.map(_.id)) += (
      None,
      bombs,
      size,
      time,
      board
    )
    database.run(games)
    println("Database save")
  }
  /*
  def loadGame() : Game = {
    val query = gameTable.take(1).result.head
    val result = database.run(query)
    val game = result.map { row =>
      val bombs = row._2
      val size = row._3
      val time = row._4
      val board = row._5
      var game = Game(bombs, size, time, board)
    }
    game
  }

  def loadField() : String = {
    val query = fieldTable.take(1).result.head
    val result = database.run(query)
    val field = result.map { row =>
      val field = row._2
    }
    field
  }
  */

}
