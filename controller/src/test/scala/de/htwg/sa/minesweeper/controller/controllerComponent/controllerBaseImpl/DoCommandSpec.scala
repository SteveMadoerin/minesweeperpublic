package de.htwg.sa.minesweeper
package controller.controllerComponent.controllerBaseImpl

import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpec

import de.htwg.sa.minesweeper.model.gameComponent.gameBaseImpl._
import de.htwg.sa.minesweeper.controller.controllerComponent.controllerBaseImpl.DoCommand
import de.htwg.sa.minesweeper.util.Move
import de.htwg.sa.minesweeper.model.gameComponent.gameBaseImpl.{Game, Field}


class DoCommandSpec extends AnyWordSpec{

    "DoCommand" should{
        val doCommand = new DoCommand(Move("open", 1, 1))
        val doCommand2 = new DoCommand(Move("flag", 1, 1))
        val doCommand3 = new DoCommand(Move("unflag", 1, 1))

        val game1 = new Game(4, 4, 0, "Playing") // check
        val sicht1 = new Matrix(4, "~")
        val unsicht1 = new Matrix(4, "8")
        

        "have no step open" in{
            val testField1 = new Field(sicht1, unsicht1)
            val sameField = doCommand.noStep(testField1)
            sameField should be(testField1)
        }

        "have do step open" in{
            val testField2 = new Field(sicht1, unsicht1)
            val stepField = doCommand.doStep(testField2)
            stepField.showVisibleCell(1, 1) should be ("8") 

        }

        "have undo step open" in{
            val testField3 = new Field(sicht1, unsicht1)
            val stepField1 = doCommand.doStep(testField3)
            val undoField = doCommand.undoStep(stepField1)

            undoField should be(testField3)
        }

        "have redo step open " in{
            val testField4 = new Field(sicht1, unsicht1)
            val stepField2 = doCommand.doStep(testField4)
            val undoField1 = doCommand.undoStep(stepField2)
            val redoField = doCommand.redoStep(undoField1)

            redoField should be(stepField2)
        }

        "have no step flag" in{
            val testField5 = new Field(sicht1, unsicht1)
            val sameField1 = doCommand2.noStep(testField5)
            sameField1 should be(testField5)
        }

        "have do step flag" in{
            val testField6 = new Field(sicht1, unsicht1)
            val stepField3 = doCommand2.doStep(testField6)
            stepField3.showVisibleCell(1, 1) should be ("F") 

        }

        "have undo step flag" in{
            val testField7 = new Field(sicht1, unsicht1)
            val stepField4 = doCommand2.doStep(testField7)
            val undoField2 = doCommand2.undoStep(stepField4)

            undoField2 should be(testField7)
        }

        "have redo step flag" in{
            val testField8 = new Field(sicht1, unsicht1)
            val stepField5 = doCommand2.doStep(testField8)
            val undoField3 = doCommand2.undoStep(stepField5)
            val redoField1 = doCommand2.redoStep(undoField3)

            redoField1 should be(stepField5)
        }

        "have no step unflag" in{
            val testField9 = new Field(sicht1, unsicht1)
            val sameField2 = doCommand3.noStep(testField9)
            sameField2 should be(testField9)
        }

        "have do step unflag" in{
            val testField10 = new Field(sicht1, unsicht1)
            val stepField6 = doCommand3.doStep(testField10)
            stepField6.showVisibleCell(1, 1) should be ("~") 

        }

        "have undo step unflag" in{
            val testField11 = new Field(sicht1, unsicht1)
            val stepField7 = doCommand2.doStep(testField11) // flag
            val undoField4 = doCommand3.doStep(stepField7) // unflag
            val undoField5 = doCommand3.undoStep(undoField4)

            undoField5 should be(stepField7)
        }

        "have redo step unflag" in{
            val testField12 = new Field(sicht1, unsicht1)
            val stepField8 = doCommand2.doStep(testField12) // flag
            val undoField6 = doCommand3.doStep(stepField8) // unflag
            val undoField7 = doCommand3.undoStep(undoField6)
            val redoField2 = doCommand3.redoStep(undoField7)

            redoField2 should be(testField12)
        }


    }

}