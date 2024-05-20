package de.htwg.sa.minesweeper.persistence.persistenceComponent.persistenceSlickImpl.Dao

import de.htwg.sa.minesweeper.persistence.persistenceComponent.persistenceSlickImpl.{FieldTable, IDao}
import slick.jdbc.JdbcBackend.Database
import slick.jdbc.PostgresProfile.api.*
import slick.lifted.TableQuery

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

class FieldDao(db: Database)  extends IDao[String, Int] {

    private val fieldTable = TableQuery(FieldTable(_))

    override def save(obj: String): String = {
        val fields = (fieldTable returning fieldTable.map(_.id)) += (
            None,
            obj
        )
        val resultFuture = db.run(fields)
        println("Database save Field")
        obj
    }

    override def findAll(): Seq[String] = {
        val query = for {
            field <- fieldTable
        } yield {
            (field)
        }
        val resultFuture = db.run(query.result)
        val resultField:Seq[(Option[Int], String)] = Await.result(resultFuture, 1.second)
        val fieldSeq: Seq[String] = resultField.map { case (field) =>
            field._2
        }
        println("Database find all Fields")
        fieldSeq
    }

    override def findById(id: Int): String = {
        val query = for {
            field <- fieldTable if field.id === id
        } yield {
            (field)
        }

        val resultFuture = db.run(query.result)
        val resultField: Seq[(Option[Int], String)] = Await.result(resultFuture, 1.second)
        val fieldSeq: Seq[String] = resultField.map { case (field) =>
            field._2
        }

        println("Database find Field by id")
        fieldSeq.head
    }


    override def update(id: Int, obj: String): String = {
        val query = fieldTable
            .filter(_.id === id)
            .map(field => (field))
            .update(Option(id), obj)
        val resultFuture = db.run(query)
        println("Database update Field")
        obj
    }

    override def delete(id: Int): Boolean = {

        val query = fieldTable
            .filter(_.id === id)
            .delete
        val resultFuture = db.run(query)
        println("Database delete Field")
        true
    }

}
