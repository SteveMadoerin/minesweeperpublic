package de.htwg.sa.minesweeper.persistence.persistenceComponent

import slick.jdbc.PostgresProfile.api._
import slick.lifted.{ForeignKey, ForeignKeyQuery}

class FieldTable(tag: Tag) extends Table[(Option[Int], String)](tag, "Field") {
  def id = column[Option[Int]]("id", O.PrimaryKey, O.AutoInc)
  def field = column[String]("field_data")

  def * = (id, field)

}
