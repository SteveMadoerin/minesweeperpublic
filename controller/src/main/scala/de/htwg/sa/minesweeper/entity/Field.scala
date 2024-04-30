package de.htwg.sa.minesweeper.entity

case class Field(matrix: Matrix[String], hidden: Matrix[String]) extends IField
