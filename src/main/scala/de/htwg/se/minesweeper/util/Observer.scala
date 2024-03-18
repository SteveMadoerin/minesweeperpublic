package de.htwg.se.minesweeper.util

trait Observer:
    def update(e: NewEvent): Boolean

trait Observable:
    var subscribers:Vector[Observer] = Vector()
    def add(s:Observer) = subscribers = subscribers :+ s
    def remove(s:Observer) = subscribers = subscribers.filterNot(o => o==s)
    def notifyObservers(e: NewEvent) = subscribers.foreach(o => o.update(e))

enum NewEvent:
    case NewGame
    case Start
    case Next
    case GameOver
    case Cheat
    case Help
    case Input
    case Load
    case Save
    case SaveTime
    case Exit