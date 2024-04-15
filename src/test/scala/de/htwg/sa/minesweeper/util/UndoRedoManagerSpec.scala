/* package de.htwg.sa.minesweeper.shared

import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpec
import de.htwg.sa.minesweeper.shared.UndoRedoManager

class UndoRedoManagerSpec extends AnyWordSpec{
    "UndoRedoManager" should{
        val undoRedoManager = new UndoRedoManager[Int]
        val command2 = new testCommand

        "doStep - undoStep -redoStep" in{
            var t = 0
            t = undoRedoManager.doStep(t, command2)
            t should be(1)

            t = undoRedoManager.undoStep(t)
            t should be(0)

            t = undoRedoManager.redoStep(t)
            t should be(1)
        }


        "manage multiple steps" in{
            var t = 0
            t = undoRedoManager.doStep(t, command2)
            t = undoRedoManager.doStep(t, command2)
            t should be(2)

            t = undoRedoManager.undoStep(t)
            t should be(1)

            t = undoRedoManager.undoStep(t)
            t should be(0)

            t = undoRedoManager.redoStep(t)
            t should be(1)
        }
    }
} */