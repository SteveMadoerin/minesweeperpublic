package de.htwg.se.minesweeper.model.gameComponent.gameBaseImpl

trait Event

case class PlayEvent() extends Event
case class WonEvent() extends Event
case class LostEvent() extends Event