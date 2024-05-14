package de.htwg.sa.minesweeper.persistence.persistenceComponent

import akka.japi.JAPI
import de.htwg.sa.minesweeper.persistence.entity.{Game, IField, IGame}
import de.htwg.sa.minesweeper.persistence.persistenceComponent.persistenceSlickImpl.Dao.{FieldDao, GameDao, PlayerScoreDao}
import de.htwg.sa.minesweeper.persistence.persistenceComponent.persistenceSlickImpl.Util
import play.api.libs.json.Json
import slick.jdbc.PostgresProfile.api.*
import slick.jdbc.JdbcBackend.Database
import slick.lifted.TableQuery
import slick.dbio.DBIO

import java.sql.SQLNonTransientException
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success}
import concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt


class Slick extends IPersistence {

  private val databaseDB: String = sys.env.getOrElse("POSTGRES_DATABASE", "postgres")
  private val databaseUser: String = sys.env.getOrElse("POSTGRES_USER", "postgres")
  private val databasePassword: String = sys.env.getOrElse("POSTGRES_PASSWORD", "Kiiing001")
  private val databasePort: String = sys.env.getOrElse("POSTGRES_PORT", "5432")
  private val databaseHost: String = sys.env.getOrElse("POSTGRES_HOST", "localhost")
  private val databaseUrl = s"jdbc:postgresql://$databaseHost:$databasePort/$databaseDB?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&autoReconnect=true"

  val database = Database.forURL(
    url = databaseUrl,
    driver = "org.postgresql.Driver",
    user = databaseUser,
    password = databasePassword
  )

  private val gameTable = TableQuery[GameTable]
  private val fieldTable = TableQuery[FieldTable]
  private val playerTable = TableQuery[PlayerScoreTable]

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
  
  def savePlayerScore(playerName: String, score: Int, filePath: String): Unit = {
    PlayerScoreDao(database).save((playerName, score))
  }

//  def savePlayerScore(playerName: String, score: Int): Unit = {
//
//    val playerscore = (playerTable returning playerTable.map(_.id)) += (
//        None,
//        score,
//        playerName
//    )
//    database.run(playerscore)
//    println("Database save")
//  }

  def saveField(field: IField): Unit = {
    FieldDao(database).save(Util.f.fieldToJson(field))
  }

//  def saveField(field: String): Unit = {
//    val fields = (fieldTable returning fieldTable.map(_.id)) += (
//        None,
//        field
//    )
//    database.run(fields)
//    println("Database save")
//  }

  def saveGame(game: IGame): Unit = {
    GameDao(database).save(game.asInstanceOf[Game])
  }

//  def saveGame(bombs: Int, size: Int, time: Int, board: String): Unit = {
//    val games = (gameTable returning gameTable.map(_.id)) += (
//        None,
//        bombs,
//        size,
//        time,
//        board
//    )
//    database.run(games)
//    println("Database save")
//  }





  def loadField: Option[IField] = {
    FieldDao(database).findById(1) match{
      case field => Some(Util.f.jsonToField(field))
      case _ => None
    }
  }


//  def loadField(): String = {
//    val query = for {
//      field <- fieldTable.sortBy(_.id.desc).take(1)
//    } yield {
//      field.field
//    }
//
//    val resultFuture = database.run(query.result)
//    val resultField: Seq[String] = Await.result(resultFuture, 1.second)
//    resultField.head
//  }

  def loadGame: Option[IGame] = {
    GameDao(database).findById(1) match
      case g => Some(g)
      case _ => None
  }

  //  def loadGame(): Game = {
  //    val query = for {
  //      spiel <- gameTable.sortBy(_.id.desc).take(1)
  //    } yield {
  //      (spiel.bombs, spiel.size, spiel.time, spiel.board)
  //    }
  //
  //    val resultFuture = database.run(query.result)
  //    val resultGame: Seq[(Int, Int, Int, String)] = Await.result(resultFuture, 1.second)
  //    val gameSeq: Seq[Game] = resultGame.map { case (bombs, size, time, board) =>
  //      Game(bombs, size, time, board)
  //    }
  //
  //    gameSeq.head
  //
  //  }
  override def loadPlayerScores(filePath: String): Seq[(String, Int)] = {
    PlayerScoreDao(database).findAll()
  }

  def closeConnection = {
    database.close
  }

  
  
}
