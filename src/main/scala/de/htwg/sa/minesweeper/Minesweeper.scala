package de.htwg.sa.minesweeper

import de.htwg.sa.minesweeper.ui.TUI
import de.htwg.sa.minesweeper.ui.gui.GUI
//import de.htwg.sa.minesweeper.Default.{given}
import de.htwg.sa.minesweeper.ui.config.Default.{given}

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
