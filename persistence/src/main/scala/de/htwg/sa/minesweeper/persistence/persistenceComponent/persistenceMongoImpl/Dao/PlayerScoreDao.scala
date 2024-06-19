package de.htwg.sa.minesweeper.persistence.persistenceComponent.persistenceMongoImpl.Dao

import de.htwg.sa.minesweeper.persistence.persistenceComponent.persistenceSlickImpl.IDao
import org.mongodb.scala._
import org.mongodb.scala.bson.BsonTransformer.TransformImmutableDocument
import org.mongodb.scala.bson.Document
import org.mongodb.scala.result._

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

class PlayerScoreDao(db: MongoDatabase) extends IDao[(String, Int), Int]  {

    private val playerScoreCollection: MongoCollection[Document] = db.getCollection("playerscore")

    override def save(obj: (String, Int)): (String, Int) = {

        def loadNextPlayerScoreId: Int = {
            val results = Await.result(playerScoreCollection.find().sort(Document("id" -> -1)).limit(1).toFuture(), 5.seconds)
            if (results.isEmpty) 1 else results.head.getInteger("id") + 1
        }

        val playerScoreId = loadNextPlayerScoreId
        val playerScoreDocument = Document(
            "id" -> playerScoreId,
            "name" -> obj._1,
            "score" -> obj._2
        )

        val insertObservable: SingleObservable[InsertOneResult] = playerScoreCollection.insertOne(playerScoreDocument)
        insertObservable.subscribe(new Observer[InsertOneResult] {
            override def onNext(result: InsertOneResult): Unit = println(s"Inserted: $result")
            override def onError(e: Throwable): Unit = println(s"Failed to insert document: ${e.getMessage}")
            override def onComplete(): Unit = println("Insertion completed.")
        })

        obj
    }

    override def findAll(): Seq[(String, Int)] = {
        val results = Await.result(playerScoreCollection.find().toFuture(), 5.seconds)
        results.map { doc =>
            (doc.getString("name"), doc.getInteger("score"))
        }
    }

    override def findById(id: Int): (String, Int) = {
        val query = Document("id" -> id)
        val results = Await.result(playerScoreCollection.find(query).toFuture(), 5.seconds)
        (results.head.getString("name"), results.head.getInteger("score"))
    }

    override def update(id: Int, obj: (String, Int)): (String, Int) = {
        val query = Document("id" -> id)
        val update = Document("$set" -> Document("name" -> obj._1, "score" -> obj._2))
        val result = Await.result(playerScoreCollection.updateOne(query, update).toFuture(), 5.seconds)
        (obj._1, obj._2)
    }

    override def delete(id: Int): Boolean = {
        val query = Document("id" -> id)
        val result = Await.result(playerScoreCollection.deleteOne(query).toFuture(), 5.seconds)
        result.wasAcknowledged
    }
}

