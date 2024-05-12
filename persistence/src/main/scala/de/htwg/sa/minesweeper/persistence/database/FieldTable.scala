package de.htwg.sa.minesweeper.persistence.database

import slick.jdbc.PostgresProfile.api.*
import slick.lifted.{ForeignKey, ForeignKeyQuery}

class FieldTable(tag: Tag) extends Table[(Option[Int], String)](tag, "Field") {
  def id = column[Option[Int]]("id", O.PrimaryKey, O.AutoInc)
  def field = column[String]("field_data")

  def * = (id, field)

}
