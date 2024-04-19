package de.htwg.sa.minesweeper.util

trait Observer:
    def update(e: Event): Boolean

trait Observable:
    var subscribers:Vector[Observer] = Vector()
    def add(s:Observer) = subscribers = subscribers :+ s
    def remove(s:Observer) = subscribers = subscribers.filterNot(o => o==s)
    def notifyObservers(e: Event) = subscribers.foreach(o => o.update(e))

enum Event:
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