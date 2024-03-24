package de.htwg.se.minesweeper.aview


import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpec
import de.htwg.se.minesweeper.controller.controllerComponent.controllerBaseImpl.Controller
import de.htwg.se.minesweeper.model.gameComponent.gameBaseImpl._
import de.htwg.se.minesweeper.util.Move
import de.htwg.se.minesweeper.util.Event
import de.htwg.se.minesweeper.Default.{given}
import java.io.PrintStream
import java.io.ByteArrayOutputStream


class TUISpec extends AnyWordSpec{
    "A TUI when initialised" should {
        val tui1 = new TUI()
        "self register with controller" in{
            tui1.controller.subscribers should contain(tui1)
        }
    }

    "A TUI with following input" should {
        val tui = new TUI()
        "run only once" in {
            val in = new java.io.ByteArrayInputStream("no\nt\ntest\n".getBytes)
            Console.withIn(in){
                tui.parseInputandPrintLoop(true)
            }
        } 

    }

    "The TUI update method" should{
        val tui2 = new TUI()
        "update or not a Event" in{
            tui2.update(Event.Cheat) should be(false)
            tui2.update(Event.Help) should be(false)
        }
    }

    "Le Tui userinX" should {
        val testTui = new TUI()
        "parse valid user input into Move objects" in {
                testTui.userInX("o0002") should equal (Some(Move("open", 0, 2)))
                testTui.userInX("f0404") should equal (Some(Move("flag", 4, 4)))
                testTui.userInX("u0203") should equal (Some(Move("unflag", 2, 3)))
                testTui.userInX("h") should equal (None)
                testTui.userInX("r") should equal (None)
                testTui.userInX("z") should equal (None)
                testTui.userInX("y") should equal (None)
                testTui.userInX("s") should equal (None)
        }

    }

    "Le Tui chooseDifficulty" should {
        
        "return the super easy difficulty when input is 0" in {
            synchronized {
                val testTui69 = new TUI()
                val in = new java.io.ByteArrayInputStream("0\n".getBytes)
                Console.withIn(in){
                    val (side, bombs) = testTui69.chooseDifficulty()
                    side should be (5)
                    bombs should be (5)
                }
            }

        }

        "return the beginner difficulty when input is 1" in {
            synchronized {
                val testTui70 = new TUI()
                val in = new java.io.ByteArrayInputStream("1\n".getBytes)
                Console.withIn(in){
                    val (side, bombs) = testTui70.chooseDifficulty()
                    side should be (9)
                    bombs should be (10)
                }
            }
        }

        "return the intermediate difficulty when input is 2" in {
            synchronized {
                val testTui71 = new TUI()
                val in = new java.io.ByteArrayInputStream("2\n".getBytes)
                Console.withIn(in){
                    val (side, bombs) = testTui71.chooseDifficulty()
                    side should be (15)
                    bombs should be (40)
                }
            }
        }

        "return the advanced difficulty when input is 3" in {
            synchronized {
                val testTui72 = new TUI()
                val in = new java.io.ByteArrayInputStream("3\n".getBytes)
                Console.withIn(in) {
                    val (side, bombs) = testTui72.chooseDifficulty()
                    side should be (19)
                    bombs should be (85)
                }
            }
        }

        

    }


}