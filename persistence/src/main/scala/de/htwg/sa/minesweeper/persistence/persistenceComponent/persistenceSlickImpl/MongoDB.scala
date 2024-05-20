package de.htwg.sa.minesweeper.persistence.persistenceComponent.persistenceSlickImpl


import de.htwg.sa.minesweeper.persistence.entity.Game
import org.mongodb.scala.{Document, MongoClient, MongoCollection, MongoDatabase, Observable}

import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success, Try}
import scala.concurrent.duration.DurationInt
import org.bson.json.JsonObject
import org.mongodb.scala.*
import org.mongodb.scala.bson.ObjectId
import org.mongodb.scala.model.Filters.*
import org.mongodb.scala.model.Updates.*
import org.mongodb.scala.model.Projections.*
import org.mongodb.scala.model.Sorts.*
import org.mongodb.scala.model.*
import org.mongodb.scala.result.InsertOneResult
import play.api.libs.json.Json






class MongoDB {

    private val databaseDB: String = sys.env.getOrElse("MONGO_DB", "mongo") // DATABASE: mongo
    private val databaseUser: String = sys.env.getOrElse("MONGO_USERNAME", "mongodb") // USER: mongodb
    private val databasePassword: String = sys.env.getOrElse("MONGO_PASSWORD", "mongodb") // PASSWORD: mongodb
    private val databasePort: String = sys.env.getOrElse("MONGO_PORT", "27017") // PORT: 27017
    private val databaseHost: String = sys.env.getOrElse("MONGO_HOST", "localhost") // HOST: localhost
    private val databaseURI: String = s"mongodb://$databaseHost:$databasePort" // mongodb://localhost:27017
    private val client: MongoClient = MongoClient(databaseURI)


    private val db: MongoDatabase = client.getDatabase(databaseDB) // DATABASE: mongo
    private val gameCollection: MongoCollection[Document] = db.getCollection("game")
    private val fieldCollection: MongoCollection[Document] = db.getCollection("field")
    private val playerScoreCollection: MongoCollection[Document] = db.getCollection("playerscore")

    private val countersCollection: MongoCollection[Document] = db.getCollection("counters") // keeps track of next id

    def createNew(): Boolean = {
        Try {
            db.createCollection("game").head()
            db.createCollection("field").head()
            db.createCollection("playerscore").head()
            db.createCollection("counters").head()
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
        // load all counters
        val results = Await.result(countersCollection.find().toFuture(), 10.seconds)
        results.size
    }

    def loadNextGameId(): Int = {
        // load all games
        val results = Await.result(gameCollection.find().toFuture(), 10.seconds)
        results.size
        if (results.isEmpty || results == null) {
            0
        } else {
            results.size
        }
    }


    def gameToDocument(game: Game, gameId: Int): Document = {
        Document(
            "id" -> gameId,
            "bombs" -> game.bombs,
            "side" -> game.side,
            "time" -> game.time,
            "board" -> game.board
        )
    }

    def insertGame(game: Game): Unit = {
        val gameCollection: MongoCollection[Document] = db.getCollection("game")
        val document: Document = gameToDocument(game, loadNextGameId() + 1)
        val insertObservable: SingleObservable[InsertOneResult] = gameCollection.insertOne(document)

        insertObservable.subscribe(new Observer[InsertOneResult] {
            override def onNext(result: InsertOneResult): Unit = {
                println(s"Inserted: $result")// This is called when the operation is successful.
            }

            override def onError(e: Throwable): Unit = {
                println(s"Failed to insert document: ${e.getMessage}")// This is called when there's an error during the operation.
            }

            override def onComplete(): Unit = {
                println("Insertion completed.")// This is called when the operation is completed, after `onNext`.
            }
        })
    }

    def loadGame(id: Int): Option[Game] = {
        val query = Document("id" -> id)
        val results = Await.result(gameCollection.find(query).toFuture(), 10.seconds)
        if (results.isEmpty || results == null) {
            None
        } else {
            Some(Game(
                results.head.getInteger("bombs"),
                results.head.getInteger("side"),
                results.head.getInteger("time"),
                results.head.getString("board")
            ))
        }
    }


/*    def loadGame(id: Int): Option[Game] = {
        val objectId = new ObjectId(id.toHexString) // Convert the Int id to an ObjectId
        val query = Filters.eq("_id", objectId)
        val resultFuture = gameCollection.find(query).first().head()

        Try(Await.result(resultFuture, 1.second)) match {
            case Success(document) if document != null =>
                val bombs = document.getInteger("bombs")
                val side = document.getInteger("side")
                val time = document.getInteger("time")
                val board = document.getString("board")
                Some(Game(bombs, side, time, board))
            case _ => None
        }
    }*/

    def dropTable(): Unit = {
        db.drop()
    }



    def closeConnection (): Unit = {
        client.close()
    }

}
