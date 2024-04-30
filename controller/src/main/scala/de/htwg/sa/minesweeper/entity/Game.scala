package de.htwg.sa.minesweeper.entity

case class Game(bombs : Int, side: Int, time: Int, board : String) extends IGame