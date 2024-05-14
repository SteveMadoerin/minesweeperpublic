package de.htwg.sa.minesweeper.persistence.database

import de.htwg.sa.minesweeper.persistence.entity.Game
import slick.dbio.{DBIO, DBIOAction, Effect, NoStream}
import slick.jdbc.JdbcBackend.Database
import slick.lifted.TableQuery
import slick.jdbc.PostgresProfile.api.*

import concurrent.duration.DurationInt
import scala.concurrent.Await

class GameDao(db: Database)  extends IDaoNew[Game, Int] {

    private val gameTable = TableQuery(GameTable(_))

    override def save(obj: Game): Game = {
        val games = (gameTable returning gameTable.map(_.id)) += (
            None,
            obj.bombs,
            obj.side,
            obj.time,
            obj.board
        )
        val resultFuture = db.run(games)
        println("Database save Game")
        obj
    }

    override def findAll(): Seq[Game] = {
        val query = for {
            spiel <- gameTable
        } yield {
            (spiel.bombs, spiel.size, spiel.time, spiel.board)
        }
        val resultFuture = db.run(query.result)
        val resultGame: Seq[(Int, Int, Int, String)] = Await.result(resultFuture, 1.second)
        val gameSeq: Seq[Game] = resultGame.map { case (bombs, size, time, board) =>
            Game(bombs, size, time, board)
        }
        println("Database find all Games")
        gameSeq
    }

    override def findById(id: Int): Game = {
        val query = for {
            spiel <- gameTable if spiel.id === id
        } yield {
            (spiel.bombs, spiel.size, spiel.time, spiel.board)
        }

        val resultFuture = db.run(query.result)
        val resultGame: Seq[(Int, Int, Int, String)] = Await.result(resultFuture, 1.second)
        val gameSeq: Seq[Game] = resultGame.map { case (bombs, size, time, board) =>
            Game(bombs, size, time, board)
        }

        println("Database find Game by id")
        gameSeq.head
    }


    override def update(id: Int, obj: Game): Game = {
        val query = gameTable
            .filter(_.id === id)
            .map(game => (game.bombs, game.size, game.time, game.board))
            .update((obj.bombs, obj.side, obj.time, obj.board))
        val resultFuture = db.run(query)
        println("Database update Game")
        obj
    }

    override def delete(id: Int): Boolean = {

        val query = gameTable
            .filter(_.id === id)
            .delete
        val resultFuture = db.run(query)
        println("Database delete Game")
        true
    }

}
