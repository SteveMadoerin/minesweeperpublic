package de.htwg.sa.minesweeper.persistence.database

import de.htwg.sa.minesweeper.model.gamecomponent.IField
import de.htwg.sa.minesweeper.model.gamecomponent.IGame

trait IDAO:
  def saveGame(game: IGame): Unit
  def saveField(field: IField): Unit
  def savePlayerScore(playerName: String, score: Int): Unit

  def loadGame(): Unit
  def loadField(): Unit
  def loadPlayerScore(): Unit
