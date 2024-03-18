/* package de.htwg.se.minesweeper.model.gameComponent.gameBaseImpl

import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpec

class SetupSpec extends AnyWordSpec{
    "it" should{
        "return the same object with multible calls in same thread" in {
            val s1 = Setup
            val s2 = Setup
            s1 should be (s2)
        }
    }

    "the object" should{
        "return a field" in {
            var game = new Game(Status.Playing)
            val result = Setup.prepare(game)
            result.get(1,1) should be (Symbols.Covered)
 
        }

        "Setup prepareTUI" should {
            "return a field" in {
                var game = new Game(Status.Playing)

                val in = new java.io.ByteArrayInputStream("Steve\n0\n".getBytes)
                Console.withIn(in){
                    val result = Setup.prepareTUI(game)
                    result.get(1,1) should be (Symbols.Covered)
                }
            }
        }

        "Setup optionToList" should {
            "return a list" in {
                val result = Setup.optionToList(Some("SuperEasy"))
                result should be (List(5, 5))
                val result2 = Setup.optionToList(Some("Easy"))
                result2 should be (List(9, 10))
                val result3 = Setup.optionToList(Some("Medium"))
                result3 should be (List(15, 40))
                val result4 = Setup.optionToList(Some("Hard"))
                result4 should be (List(19, 85))
                val result5 = Setup.optionToList(Some("Test"))
                result5 should be (List(9, 10))
            }
        }

        "Setup prepareBoard" should {
            "return a field" in {
                var game = new Game(Status.Playing)
                val result = Setup.prepareBoard(Some("SuperEasy"), game)
                result.get(1,1) should be (Symbols.Covered)
            }
        }




    }
} */