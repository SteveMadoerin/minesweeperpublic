package de.htwg.sa.minesweeper

import aview.TUI
import aview.gui.GUI
//import de.htwg.sa.minesweeper.Default.{given}
import aview.config.Default.{given}

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
