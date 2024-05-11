package de.htwg.sa.minesweeper.persistence.database

import slick.jdbc.PostgresProfile.api.*
import slick.lifted.{ForeignKey, ForeignKeyQuery}

class PlayerScoreTable extends Table[(Option[Int], String, Int)]("PlayerScore") {
  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def score = column[Int]("score")
  def name = column[String]("name")

  def * = (id, score, name)
}