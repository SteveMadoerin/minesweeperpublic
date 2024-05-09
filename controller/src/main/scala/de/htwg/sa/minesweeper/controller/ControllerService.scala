package de.htwg.sa.minesweeper.controller

import de.htwg.sa.minesweeper.ControllerApi
import de.htwg.sa.minesweeper.controller.controllerComponent.config.Default.{given}

import scala.io.StdIn.readLine

object ControllerService {
  def main(args: Array[String]): Unit = {
    ControllerApi().start() // on Port 9081
    println("Press RETURN to stop...")
    readLine() // Keep the application alive until user presses return
  }
}
