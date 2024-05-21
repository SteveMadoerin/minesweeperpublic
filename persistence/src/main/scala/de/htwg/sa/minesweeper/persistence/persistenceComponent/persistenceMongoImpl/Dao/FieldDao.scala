package de.htwg.sa.minesweeper.persistence.persistenceComponent.persistenceMongoImpl.Dao

import org.mongodb.scala.bson.Document
import de.htwg.sa.minesweeper.persistence.entity.{Field, Matrix}
import de.htwg.sa.minesweeper.persistence.persistenceComponent.persistenceSlickImpl.IDao
import org.mongodb.scala.*
import org.mongodb.scala.bson.conversions.Bson
import org.mongodb.scala.model.Filters.*
import org.mongodb.scala.model.Updates.*
import org.mongodb.scala.result.*
import org.mongodb.scala.bson.BsonTransformer.TransformImmutableDocument

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import scala.util.{Failure, Success, Try}

class FieldDao(db: MongoDatabase) extends IDao[Field, Int] {

    private val fieldCollection: MongoCollection[Document] = db.getCollection("field")

    def loadNextFieldId: Int = {
        val results = Await.result(fieldCollection.find().toFuture(), 5.seconds)
        val idNew = Try(results.maxBy(_.getInteger("id")).getInteger("id") + 1)
        idNew match
            case Success(id) => id
            case Failure(_) => 1
    }

    override def save(obj: Field): Field = {
        
        def fieldToDocument(field: Field, fieldId: Int): Document = {
            val json = field.fieldToJson(field)
            val fieldJson = Document(json)
            Document(
                "id" -> fieldId,
                "field" -> fieldJson
            )
        }

        val document: Document = fieldToDocument(obj, loadNextFieldId)
        val insertObservable: SingleObservable[InsertOneResult] = fieldCollection.insertOne(document)

        insertObservable.subscribe(new Observer[InsertOneResult] {
            override def onNext(result: InsertOneResult): Unit = println(s"Inserted: $result")
            override def onError(e: Throwable): Unit = println(s"Failed to insert document: ${e.getMessage}")
            override def onComplete(): Unit = println("Insertion completed.")
        })

        obj
    }
    
    override def findAll(): Seq[Field] = {
        val exampleField = Field(Matrix[String](Vector.empty), Matrix[String](Vector.empty))
        val results = Await.result(fieldCollection.find().toFuture(), 5.seconds)
        results.map { doc =>
            val json = doc.get("field").get
            val field = exampleField.jsonToField(json.toString)
            field.asInstanceOf[Field]
        }
    }

    override def findById(id: Int): Field = {
        val query = Document("id" -> id)
        val results = Await.result(fieldCollection.find(query).toFuture(), 5.seconds)
        results.headOption match {
            case Some(doc) =>
                val json = doc.get("field").get
                val field = Field(Matrix[String](Vector.empty), Matrix[String](Vector.empty))
                field.jsonToField(json.toString).asInstanceOf[Field]
            case None =>
                Field(Matrix[String](Vector.empty), Matrix[String](Vector.empty))
        }
    }

    override def update(id: Int, obj: Field): Field = {
        val filter: Bson  = equal("id", id)
        val update: Bson = combine(set("field", obj.fieldToJson(obj)))
        val result = Await.result(fieldCollection.updateOne(filter, update).toFuture(), 5.seconds)
        obj
    }

    override def delete(id: Int): Boolean = {
        val query = Document("id" -> id)
        val result = Await.result(fieldCollection.deleteOne(query).toFuture(), 5.seconds)
        result.getDeletedCount == 1
    }
}

