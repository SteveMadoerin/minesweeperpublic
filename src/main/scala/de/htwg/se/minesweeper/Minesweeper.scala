package de.htwg.se.minesweeper

import de.htwg.se.minesweeper.aview.TUI
import de.htwg.se.minesweeper.aview.gui.GUI
import de.htwg.se.minesweeper.Default.{given}

object Minesweeper {
    
    def main(args: Array[String]): Unit = {

        if (args.length >= 1){
            return
        } else {
            
            GUI().run
            TUI().run
        }
        
    }
}
