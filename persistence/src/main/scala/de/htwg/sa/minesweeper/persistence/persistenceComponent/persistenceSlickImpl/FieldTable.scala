package de.htwg.sa.minesweeper.persistence.persistenceComponent.persistenceSlickImpl

import slick.jdbc.PostgresProfile.api._

class FieldTable(tag: Tag) extends Table[(Option[Int], String)](tag, "Field") {
  def id = column[Option[Int]]("id", O.PrimaryKey, O.AutoInc)
  def field = column[String]("field_data")

  def * = (id, field)

}
