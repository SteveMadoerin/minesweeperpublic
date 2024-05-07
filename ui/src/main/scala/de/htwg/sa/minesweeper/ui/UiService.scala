package de.htwg.sa.minesweeper.ui

import de.htwg.sa.minesweeper.ui._
/* import de.htwg.sa.minesweeper.ui.config.Default.{given} */

object UiService:

  @main def main(): Unit = 
    //TUI().start // on Port 8081 //Controller.().start
    //TUI().run 
    TUI().bindFuture
    TUI().run


end UiService
