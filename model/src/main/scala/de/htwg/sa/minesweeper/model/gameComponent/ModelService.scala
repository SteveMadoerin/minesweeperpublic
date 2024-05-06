package de.htwg.sa.minesweeper.model.gameComponent

import de.htwg.sa.minesweeper.model.gameComponent.ModelApi
import de.htwg.sa.minesweeper.model.gameComponent.IGame
import de.htwg.sa.minesweeper.model.gameComponent.config.Default.{given}
import de.htwg.sa.minesweeper.model.gameComponent._
import de.htwg.sa.minesweeper.model.gameComponent.gameBaseImpl._

object ModelService:

  @main def main(): Unit = ModelApi().start // on Port 8081 //Controller.().start

end ModelService
