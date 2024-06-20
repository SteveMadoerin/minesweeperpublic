package de.htwg.sa.minesweeper.ui.gui

import org.scalatest._
import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpec



class GUISpec extends AnyWordSpec{
     "a gui" when {
        val guiTest = new GUI()
        guiTest.visible = false
        "initialised" should {
            "have a title" in {
                guiTest.title should be ("Minesweeper")
            }
        }
        guiTest.close()
    } 
    "a GUI" should {
        "run" in {
            
        }
        
    }
}