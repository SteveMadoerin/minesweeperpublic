package de.htwg.sa.minesweeper.ui

import de.htwg.sa.minesweeper.ui.gui.GUI

import scala.io.StdIn.readLine

object UiService {
    def main(args: Array[String]): Unit = {
        val gui = GUI()
        val tui = TUI()
        gui.start // on Port 9087
        tui.start // on Port 9088
        gui.run
        tui.run
        
        println("Press RETURN to stop...")
        readLine()
    }

}
