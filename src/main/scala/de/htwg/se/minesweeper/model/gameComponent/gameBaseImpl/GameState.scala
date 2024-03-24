package de.htwg.se.minesweeper.model.gameComponent.gameBaseImpl

import de.htwg.se.minesweeper.model.gameComponent._
import de.htwg.se.minesweeper.Default


object GameState{

    //Initial state
    val initialState: Status = Playing

    def handle(e: Event,currentState: Status): Status = {
        e match{
            case _: PlayEvent => playState(currentState)
            case _: WonEvent => Won//wonState
            case _: LostEvent => Lost//lostState
        }
    }

    def playState(currentSate: Status): Status =
        println("")
        currentSate

    def wonState: Status =
        println(s"Game is won") 
        Won

    def lostState: Status =
        println(s"Game is lost ")
        Lost

}