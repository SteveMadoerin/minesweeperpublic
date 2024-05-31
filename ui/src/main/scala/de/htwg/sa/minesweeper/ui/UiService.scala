package de.htwg.sa.minesweeper.ui

import de.htwg.sa.minesweeper.ui.gui.GUI

import scala.io.StdIn.readLine

object UiService {
    def main(args: Array[String]): Unit = {
        WebGuiApi().start() // on Port 9080
        val gui = GUI()
        val tui = TUI()
        gui.start() // on Port 9087
        tui.start() // on Port 9088
        gui.run
        tui.run

        //GUI().run // on Port 9087
        //TUI().run // on Port 9088
        println("Press RETURN to stop...")
        readLine() // Keep the application alive until user presses return
    }

}
