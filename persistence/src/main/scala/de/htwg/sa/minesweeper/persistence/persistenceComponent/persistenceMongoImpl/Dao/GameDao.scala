package de.htwg.sa.minesweeper.persistence.persistenceComponent.persistenceMongoImpl.Dao

import de.htwg.sa.minesweeper.persistence.entity.Game
import de.htwg.sa.minesweeper.persistence.persistenceComponent.persistenceSlickImpl.IDao
import org.mongodb.scala.*
import org.mongodb.scala.bson.conversions.Bson
import org.mongodb.scala.model.Filters.*
import org.mongodb.scala.model.Updates.*
import org.mongodb.scala.result.*

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import scala.util.{Failure, Success, Try}

class GameDao(db: MongoDatabase) extends IDao[Game, Int] {

    val gameCollection: MongoCollection[Document] = db.getCollection("game")

    def loadNextGameId: Int = {
        val results = Await.result(gameCollection.find().toFuture(), 5.seconds)
        val idNew = Try(results.maxBy(_.getInteger("id")).getInteger("id") + 1)
        idNew match
            case Success(id) => id
            case Failure(_) => 1
    }

    override def save(obj: Game): Game = {
        
        def gameToDocument(game: Game, gameId: Int): Document = {
            Document(
                "id" -> gameId,
                "bombs" -> game.bombs,
                "side" -> game.side,
                "time" -> game.time,
                "board" -> game.board
            )
        }

        val document: Document = gameToDocument(obj, loadNextGameId) // what if a game gets deleted?
        val insertObservable: SingleObservable[InsertOneResult] = gameCollection.insertOne(document)

        insertObservable.subscribe(new Observer[InsertOneResult] {
            override def onNext(result: InsertOneResult): Unit = {
                println(s"Inserted: $result") // This is called when the operation is successful.
            }

            override def onError(e: Throwable): Unit = {
                println(s"Failed to insert document: ${e.getMessage}") // This is called when there's an error during the operation.
            }

            override def onComplete(): Unit = {
                println("Insertion completed.") // This is called when the operation is completed, after `onNext`.
            }
        })
        
        obj

    }

    override def findAll(): Seq[Game] = {
        val results = Await.result(gameCollection.find().toFuture(), 5.seconds)
        results.map { doc =>
            Game(
                doc.getInteger("bombs"),
                doc.getInteger("side"),
                doc.getInteger("time"),
                doc.getString("board")
            )
        }
    }


    override def findById(id: Int): Game = {
        val query = Document("id" -> id)
        val results = Await.result(gameCollection.find(query).toFuture(), 5.seconds)
        results.headOption match {
            case Some(doc) => Game(
                doc.getInteger("bombs"),
                doc.getInteger("side"),
                doc.getInteger("time"),
                doc.getString("board")
            )
            case None => Game(0, 0, 0, "")
        }
    }



    override def update(id: Int, obj: Game): Game = {
        val filter: Bson  = equal("id", id) 
        val update: Bson  = combine(
            set("bombs", obj.bombs),
            set("side", obj.side),
            set("time", obj.time),
            set("board", obj.board)
        )

        gameCollection.updateOne(filter, update).subscribe(
            (updateResult: UpdateResult) => println(s"Updated: ${updateResult.getModifiedCount} document(s)"),
            (t: Throwable) => println(s"Failed: ${t.getMessage}"),
            () => println("Completed update operation")
        )
        obj
    }

    override def delete(id: Int): Boolean = {
        val result = Await.result(gameCollection.deleteOne(equal("id", id)).toFuture(), 5.seconds)
        result.getDeletedCount > 0
    }
}
