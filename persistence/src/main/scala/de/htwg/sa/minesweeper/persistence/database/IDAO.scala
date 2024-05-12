package de.htwg.sa.minesweeper.persistence.database

import de.htwg.sa.minesweeper.persistence.entity._

trait IDAO:
  def saveGame(bombs: Int, size: Int, time: Int, board: String): Unit
  def saveField(field: String): Unit
  def savePlayerScore(playerName: String, score: Int): Unit

  //def loadGame(): Game
  //def loadField(): String
  //def loadPlayerScore(): Unit
