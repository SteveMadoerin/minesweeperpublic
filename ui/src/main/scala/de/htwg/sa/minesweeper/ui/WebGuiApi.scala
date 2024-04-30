package de.htwg.sa.minesweeper.ui


import de.htwg.sa.minesweeper.controller.controllerComponent.IController
import de.htwg.sa.minesweeper.util.Observer
import de.htwg.sa.minesweeper.util.Event


case class WebGuiApi(controller: IController) extends Observer{

  override def update(e: Event): Boolean = ???



}
