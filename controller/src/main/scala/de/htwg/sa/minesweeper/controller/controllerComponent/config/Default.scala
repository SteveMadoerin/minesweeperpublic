package de.htwg.sa.minesweeper.controller.controllerComponent.config

import de.htwg.sa.minesweeper.util.{Observable, Move, UndoRedoManager, Event}
import de.htwg.sa.minesweeper.controller.controllerComponent.IController
import de.htwg.sa.minesweeper.controller.controllerComponent.controllerBaseImpl.Controller

object Default{
    given IController = Controller()
}
