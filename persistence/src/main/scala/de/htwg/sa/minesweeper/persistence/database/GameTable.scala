package de.htwg.sa.minesweeper.persistence.database

import slick.jdbc.PostgresProfile.api.*
import slick.lifted.{ForeignKey, ForeignKeyQuery}

class GameTable extends Table[(Option[Int], String)]("Game") {
  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def game = column[String]("game_data")

  def * = (id, game)

}
