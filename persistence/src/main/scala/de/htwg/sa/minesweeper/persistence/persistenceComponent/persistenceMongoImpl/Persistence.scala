package de.htwg.sa.minesweeper.persistence.persistenceComponent.persistenceMongoImpl

import de.htwg.sa.minesweeper.persistence.entity.{Game, IField, IGame}
import de.htwg.sa.minesweeper.persistence.persistenceComponent.IPersistence
import de.htwg.sa.minesweeper.persistence.persistenceComponent.persistenceMongoImpl.Dao.{FieldDao, GameDao, PlayerScoreDao}
import org.mongodb.scala.*

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import scala.util.{Failure, Success, Try}

/* MongoDB implementation of IPersistence */
class Persistence extends IPersistence {

    private val databaseDB: String = sys.env.getOrElse("MONGO_DB", "mongo") // DATABASE: mongo
    private val databaseUser: String = sys.env.getOrElse("MONGO_USERNAME", "mongodb") // USER: mongodb
    private val databasePassword: String = sys.env.getOrElse("MONGO_PASSWORD", "mongodb") // PASSWORD: mongodb
    private val databasePort: String = sys.env.getOrElse("MONGO_PORT", "27017") // PORT: 27017
    private val databaseHost: String = sys.env.getOrElse("MONGO_HOST", "localhost") // HOST: localhost
    private val databaseURI: String = s"mongodb://$databaseHost:$databasePort" // mongodb://localhost:27017
    private val client: MongoClient = MongoClient(databaseURI)


    val db: MongoDatabase = client.getDatabase(databaseDB) // DATABASE: mongo
    private val gameCollection: MongoCollection[Document] = db.getCollection("game")
    private val fieldCollection: MongoCollection[Document] = db.getCollection("field")
    private val playerScoreCollection: MongoCollection[Document] = db.getCollection("playerscore")
    

    def createNew(): Boolean = {
        Try {
            db.createCollection("game").head()
            db.createCollection("gameCounter").head()
            db.createCollection("field").head()
            db.createCollection("playerscore").head()
            true
        } match
            case Failure(exception) =>
                println(exception)
                false
            case Success(value) =>
                println(value)
                true
    }

    def loadCounters(): Int = {
        val countersCollection: MongoCollection[Document] = db.getCollection("gameCounter")
        val results = Await.result(countersCollection.find().toFuture(), 10.seconds)
        results.size
    }
    


    private def gameToDocument(game: Game, gameId: Int): Document = {
        Document(
            "id" -> gameId,
            "bombs" -> game.bombs,
            "side" -> game.side,
            "time" -> game.time,
            "board" -> game.board
        )
    }
    

    def dropTable(): Unit = {
        db.drop()
    }

    def closeConnection (): Unit = {
        client.close()
    }

    override def loadGame: Option[IGame] = {
        Try(GameDao(db).findById(0)) match {
            case Success(game) => Some(game)
            case Failure(_) => None
        }
    }

    override def saveGame(game: IGame): Unit = {
        GameDao(db).save(game.asInstanceOf[Game])
    }

    override def loadField: Option[IField] = {
        Try(FieldDao(db).findById(0)) match {
            case Success(field) => Some(field)
            case Failure(_) => None
        }
    }

    override def saveField(field: IField): Unit = {
        FieldDao(db).save(field.asInstanceOf[de.htwg.sa.minesweeper.persistence.entity.Field])
    }

    override def loadPlayerScores(filePath: String): Seq[(String, Int)] = {
        PlayerScoreDao(db).findAll()
    }

    override def savePlayerScore(playerName: String, score: Int, filePath: String): Unit = {
        PlayerScoreDao(db).save((playerName, score))
    }
}
