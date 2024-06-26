package de.htwg.sa.minesweeper.persistence.persistenceComponent.persistenceSlickImpl

import slick.jdbc.PostgresProfile.api._

class GameTable(tag: Tag) extends Table[(Option[Int], Int, Int, Int, String)](tag, "Game") {
  def id = column[Option[Int]]("id", O.PrimaryKey, O.AutoInc)
  def bombs = column[Int]("bombs")
  def size = column[Int]("size")
  def time = column[Int]("time")
  def board = column[String]("board")
  def * = (id, bombs, size, time, board)

}
