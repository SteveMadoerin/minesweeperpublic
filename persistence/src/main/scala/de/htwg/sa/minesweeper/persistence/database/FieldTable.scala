package de.htwg.sa.minesweeper.persistence.database

import slick.jdbc.PostgresProfile.api.*
import slick.lifted.{ForeignKey, ForeignKeyQuery}

class FieldTable extends Table[(Option[Int], String)]("Field") {
  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def field = column[String]("field_data")

  def * = (id, field)

}
