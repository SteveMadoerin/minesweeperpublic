import de.htwg.sa.minesweeper.controller.controllerComponent.IController
import de.htwg.sa.minesweeper.ControllerApi
import de.htwg.sa.minesweeper.controller.controllerComponent.config.Default.{given}
import de.htwg.sa.minesweeper.controller.controllerComponent.config.Default

object ControllerService:

    @main def main(): Unit = ControllerApi().start // on Port 8081 //Controller.().start

end ControllerService

