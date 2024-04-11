package de.htwg.sa.minesweeper.util

import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpec
import de.htwg.sa.minesweeper.util.Command

class testCommand extends  Command[Int]:
    override def noStep(t: Int): Int = t
    override def doStep(t: Int): Int = t + 1
    override def undoStep(t: Int): Int = t - 1
    override def redoStep(t: Int): Int = t + 1

class CommandSpec extends AnyWordSpec{
    "Command" should{
        val command = new testCommand

        "have no step" in{
            command.noStep(1) should be(1)
        }

        "have do step" in{
            command.doStep(1) should be(2)
        }

        "have undo step" in{
            command.undoStep(1) should be(0)
        }

        "have redo step" in{
            command.redoStep(1) should be(2)
        }
    }


}
    	