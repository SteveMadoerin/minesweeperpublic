package de.htwg.sa.minesweeper.controller

import de.htwg.sa.minesweeper.{ControllerApi, ControllerCommunication}
import de.htwg.sa.minesweeper.controller.controllerComponent.config.Default.given

import scala.io.StdIn.readLine

object ControllerService {
  def main(args: Array[String]): Unit = {
    //ControllerCommunication()
    ControllerApi().start() // on Port 9081
    println("Press RETURN to stop...")
    readLine()
  }
}
