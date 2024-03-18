package de.htwg.se.minesweeper.model.gameComponent.gameBaseImpl

import de.htwg.se.minesweeper.model.gameComponent._
import de.htwg.se.minesweeper.Default


object GameState{

    var state = playState

    def handle(e: Event) = {
        e match{
            case play: PlayEvent => state = playState
            case won: WonEvent => state = wonState
            case lost: LostEvent => state = lostState
        }
    }

    def playState = 
        println("")
        Status.Playing

    def wonState = 
        println(s"Game is won") 
        Status.Won

    def lostState =
        println(s"Game is lost ")
        Status.Lost

}