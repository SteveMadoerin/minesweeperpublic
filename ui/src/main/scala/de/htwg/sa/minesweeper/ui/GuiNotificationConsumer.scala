package de.htwg.sa.minesweeper.ui

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Directives.complete
import akka.kafka.scaladsl.Consumer
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.stream.Materializer
import akka.stream.Materializer.matFromSystem
import akka.stream.scaladsl.Sink
import de.htwg.sa.minesweeper.ui.model.{Event, GameTui}
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.{StringDeserializer, StringSerializer}

import scala.concurrent.Future

class GuiNotificationConsumer(system: ActorSystem)(update : Event => Unit)(implicit val materializer: Materializer) {
    val GuiCommandTopic = "gui-notify"

    private val consumerSettings = ConsumerSettings(system, new StringDeserializer, new StringDeserializer)
        .withBootstrapServers("broker:29092")
        .withGroupId("test-group")
        .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

    // Method to start consuming
    def startConsuming(): Unit = {
        Consumer.plainSource(consumerSettings, Subscriptions.topics(GuiCommandTopic))
            .mapAsync(1)(record => processCommand(record.value()))
            .runWith(Sink.ignore)(Materializer.matFromSystem(system))
    }

    // Method to process the command
    private def processCommand(event: String): Future[Unit] = {
        val executedCommand = event match {
            case "NewGame" => update(Event.NewGame)
            case "Start" => update(Event.Start)
            case "Next" => update(Event.Next)
            case "GameOver" => update(Event.GameOver)
            case "Cheat" => update(Event.Cheat)
            case "Help" => update(Event.Help)
            case "Input" => update(Event.Input)
            case "Load" => update(Event.Load)
            case "Save" => update(Event.Save)
            case "SaveTime" => update(Event.SaveTime)
            case "Exit" => update(Event.Exit)
            case _ => false

        }
        println("GUI: executedCommand: " + executedCommand)
        val response: String = s"response: $executedCommand"
        //val game = gameService.startNewGame(startGameCommand.bombs, startGameCommand.size, startGameCommand.time)
        // After processing, you can produce a response to a response topic
        // TODO: uncomment to send a response
/*        val gameResponseProducer = new ModelResponseProducer(system)
        gameResponseProducer.sendResponse(response)*/

        Future.successful(())
    }
}
