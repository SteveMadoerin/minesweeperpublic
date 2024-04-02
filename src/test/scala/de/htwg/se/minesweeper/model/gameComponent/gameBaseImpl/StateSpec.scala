/* package de.htwg.se.minesweeper.model.gameComponent.gameBaseImpl

import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpec
import java.io.ByteArrayOutputStream
import de.htwg.se.minesweeper.Default

class StateSpec extends AnyWordSpec{

    "it" should{
        "return the same object with multible calls in same thread" in {
            val s1 = GameState
            val s2 = GameState
            s1 should be (s2)
        }
    }

    "it" should {
        "Output the correct String according to state" in {

            val testState = new PlayEvent()
            testState.toString should be ("PlayEvent()")

            val testState2 = new WonEvent()
            testState2.toString should be ("WonEvent()")

            val testState3 = new LostEvent()
            testState3.toString should be ("LostEvent()")

            val buffer = new ByteArrayOutputStream()
            Console.withOut(buffer) {
                GameState.handle(WonEvent())
            }
        }
    }

    "GameBoard" should {
        val test32 = GameState

        "be initialise correctly" in {
            test32.state = Status.Playing
            test32.state should be (Status.Playing)
        }
    }

    "GameBoard method" should {
        val test33 = GameState

        "handle the event correctly" in {
            GameState.handle(PlayEvent())
            GameState.state should be (Status.Playing)
            GameState.state = Status.Won
            test33.handle(WonEvent())
            test33.state should be (GameState.state)
            test33.handle(LostEvent())
            test33.state should be (Status.Lost)
        }
    }
    
} */