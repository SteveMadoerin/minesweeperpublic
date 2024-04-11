package de.htwg.sa.minesweeper

import de.htwg.sa.minesweeper.aview.TUI
import de.htwg.sa.minesweeper.aview.gui.GUI
import de.htwg.sa.minesweeper.Default.{given}

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
