package de.htwg.sa.minesweeper.entity

case class FieldDTO(matrix: MatrixDTO[String], hidden: MatrixDTO[String]) extends IFieldDTO
