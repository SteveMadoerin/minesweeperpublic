package de.htwg.sa.minesweeper.persistence.persistenceComponent.persistenceSlickImpl.Dao


import de.htwg.sa.minesweeper.persistence.persistenceComponent.persistenceSlickImpl.{IDao, PlayerScoreTable}
import slick.jdbc.JdbcBackend.Database
import slick.jdbc.PostgresProfile.api._
import slick.lifted.TableQuery
import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

class PlayerScoreDao(db: Database)  extends IDao[(String, Int), Int] {

    private val playerScoreTable = TableQuery(PlayerScoreTable(_))

    override def save(obj: (String, Int)): (String, Int) = {
        val playerScore = (playerScoreTable returning playerScoreTable.map(_.id)) += (
            None,
            obj._2,
            obj._1
        )
        val resultFuture = db.run(playerScore)
        println("Database save Player Score")
        obj
    }

    override def findAll(): Seq[(String, Int)] = {
        val query = for {
            playerScore <- playerScoreTable
        } yield {
            (playerScore)
        }
        val resultFuture = db.run(query.result)
        val resultPlayerScore: Seq[(Option[Int], Int, String)] = Await.result(resultFuture, 1.second)
        val playerScoreSeq: Seq[(String, Int)]  = resultPlayerScore.map { case (playerScore) =>
            (playerScore._3, playerScore._2)

        }
        println("Database find all Player Scores")
        playerScoreSeq
    }

    override def findById(id: Int): (String, Int) = {
        val query = for {
            playerScore <- playerScoreTable if playerScore.id === id
        } yield {
            (playerScore)
        }

        val resultFuture = db.run(query.result)
        val resultPlayerScore: Seq[(Option[Int], Int, String)] = Await.result(resultFuture, 1.second)
        val fieldSeq: Seq[(String, Int)] = resultPlayerScore.map { case (playerScore) =>
            (playerScore._3, playerScore._2)
        }

        println("Database find Player Score by id")
        fieldSeq.head
    }

    override def update(id: Int, obj: (String, Int)): (String, Int) = {
        val query = playerScoreTable
            .filter(_.id === id)
            .map(playerScore => (playerScore))
            .update(Option(id), obj._2, obj._1)
        val resultFuture = db.run(query)
        println("Database update Player Score")
        obj
    }

    override def delete(id: Int): Boolean = {

        val query = playerScoreTable
            .filter(_.id === id)
            .delete
        val resultFuture = db.run(query)
        println("Database delete Player Score")
        true
    }

}

