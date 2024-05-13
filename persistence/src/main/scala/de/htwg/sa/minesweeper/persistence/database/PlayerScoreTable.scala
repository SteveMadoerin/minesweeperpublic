package de.htwg.sa.minesweeper.persistence.database

import slick.jdbc.PostgresProfile.api._
import slick.lifted.{ForeignKey, ForeignKeyQuery}

class PlayerScoreTable(tag: Tag) extends Table[(Option[Int], Int, String)](tag, "PlayerScore") {
  def id = column[Option[Int]]("id", O.PrimaryKey, O.AutoInc)
  def score = column[Int]("score")
  def name = column[String]("name")

  def * = (id, score, name)
}