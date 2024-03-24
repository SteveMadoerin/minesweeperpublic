package de.htwg.se.minesweeper.model.gameComponent.gameBaseImpl

trait Status

case object Playing extends Status
case object Won extends Status
case object Lost extends Status

/*enum Status:
    case Playing, Won, Lost*/