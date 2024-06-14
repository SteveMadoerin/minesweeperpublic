package de.htwg.sa.minesweeper

import scala.concurrent.ExecutionContextExecutor
import akka.actor.ActorSystem
import de.htwg.sa.minesweeper.controller.controllerComponent.IController
import de.htwg.sa.minesweeper.util.{Event, Observer}

class ControllerCommunication (using var controller: IController) extends Observer {

    implicit val system: ActorSystem = ActorSystem()
    implicit val executionContext: ExecutionContextExecutor = system.dispatcher


    val ModelCommandTopic = "model-command"
    val ControllerCommandTopic = "controller-command"
    val GuiCommandTopic = "gui-command"
    val TuiCommandTopic = "tui-command"


    val gameCommandProducer = new ControllerCommandProducer(system)
    val gameCommandConsumer = new ControllerCommandConsumer(system)

    gameCommandConsumer.startConsuming()

    //val gameResponseProducer = new ControllerResponseProducer(system)
    val gameResponseConsumer = new ControllerResponseConsumer2(system)

    gameResponseConsumer.startConsuming()

    gameCommandProducer.sendCommand("/model/game", ModelCommandTopic)
    //gameCommandProducer.sendCommand("irgendwas Controller", ControllerCommandTopic)



    override def update(e: Event): Boolean = ???
}
