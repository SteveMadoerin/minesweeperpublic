package de.htwg.sa.minesweeper.persistence.persistenceComponent

import de.htwg.sa.minesweeper.persistence.entity.{Game, IField, IGame}
import de.htwg.sa.minesweeper.persistence.persistenceComponent.persistenceSlickImpl.Dao.{FieldDao, GameDao, PlayerScoreDao}
import de.htwg.sa.minesweeper.persistence.persistenceComponent.persistenceSlickImpl.Util
import slick.dbio.DBIO
import slick.jdbc.JdbcBackend.Database
import slick.jdbc.PostgresProfile.api.*
import slick.lifted.TableQuery

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}


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
  
  def saveField(field: IField): Unit = {
    FieldDao(database).save(Util.f.fieldToJson(field))
  }
  
  def saveGame(game: IGame): Unit = {
    GameDao(database).save(game.asInstanceOf[Game])
  }
  
  def loadField: Option[IField] = {
    FieldDao(database).findById(1) match{
      case field => Some(Util.f.jsonToField(field))
      case _ => None
    }
  }
  
  def loadGame: Option[IGame] = {
    GameDao(database).findById(1) match
      case g => Some(g)
      case _ => None
  }
  
  override def loadPlayerScores(filePath: String): Seq[(String, Int)] = {
    PlayerScoreDao(database).findAll()
  }
  
  def closeConnection = {
    database.close
  }
  
}
