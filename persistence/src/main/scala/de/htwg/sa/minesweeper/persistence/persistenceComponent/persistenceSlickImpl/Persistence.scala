package de.htwg.sa.minesweeper.persistence.persistenceComponent.persistenceSlickImpl

import de.htwg.sa.minesweeper.persistence.entity.{Game, IField, IGame}
import de.htwg.sa.minesweeper.persistence.persistenceComponent.IPersistence
import de.htwg.sa.minesweeper.persistence.persistenceComponent.persistenceSlickImpl.Dao.{FieldDao, GameDao, PlayerScoreDao}
import slick.dbio.DBIO
import slick.jdbc.JdbcBackend.Database
import slick.jdbc.PostgresProfile.api._
import slick.lifted.TableQuery

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

class Persistence extends IPersistence {

  private val logger = org.slf4j.LoggerFactory.getLogger(classOf[Persistence])

  private val databaseDB: String = sys.env.getOrElse("POSTGRES_DATABASE", "postgres")
  private val databaseUser: String = sys.env.getOrElse("POSTGRES_USER", "postgres")
  private val databasePassword: String = sys.env.getOrElse("POSTGRES_PASSWORD", "Kiiing001")
  private val databasePort: String = sys.env.getOrElse("POSTGRES_PORT", "5432")
  //private val databaseHost: String = sys.env.getOrElse("POSTGRES_HOST", "localhost")
  private val databaseHost: String = sys.env.getOrElse("POSTGRES_HOST", "host.docker.internal")
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

  database.run(setup).onComplete {
    case Success(value) => logger.info("Tables created")
    case Failure(exception) => logger.error(exception.getMessage)
  }
  
  def savePlayerScore(playerName: String, score: Int, filePath: String): Unit = PlayerScoreDao(database).save((playerName, score))
  
  def saveField(field: IField): Unit = FieldDao(database).save(Util.f.fieldToJson(field))
  
  def saveGame(game: IGame): Unit = GameDao(database).save(game.asInstanceOf[Game])
  
  def loadField: Option[IField] = {
    FieldDao(database).findById(1) match{
      case field => Some(Util.f.jsonToField(field))
    }
  }
  
  def loadGame: Option[IGame] = {
    GameDao(database).findById(1) match
      case g => Some(g)
  }
  
  override def loadPlayerScores(filePath: String): Seq[(String, Int)] = PlayerScoreDao(database).findAll()
  
  def closeConnection = database.close

  def dropTables(): Unit = {
    val shutdownSetup: DBIOAction[Unit, NoStream, Effect.Schema] = DBIO.seq(
      gameTable.schema.dropIfExists,
      fieldTable.schema.dropIfExists,
      playerTable.schema.dropIfExists
    )
    database.run(shutdownSetup).onComplete {
      case Success(value) => logger.info("Tables dropped")
      case Failure(exception) => logger.error(exception.getMessage)
    }
  }
  
}
