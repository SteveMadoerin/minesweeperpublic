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

class TuiNotificationConsumer(system: ActorSystem)(update : Event => Unit)(implicit val materializer: Materializer) {

    val TuiCommandTopic = "tui-notify"

    private val consumerSettings = ConsumerSettings(system, new StringDeserializer, new StringDeserializer)
        .withBootstrapServers("broker:29092")
        .withGroupId("test-group")
        .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

    def startConsuming(): Unit = {
        Consumer.plainSource(consumerSettings, Subscriptions.topics(TuiCommandTopic))
            .mapAsync(1)(record => processCommand(record.value()))
            .runWith(Sink.ignore)(Materializer.matFromSystem(system))
    }

    private def processCommand(event: String): Future[Unit] = {
        val executedCommand = event match {
            case "NewGame" => update(Event.NewGame); "NewGame"
            case "Start" => update(Event.Start); "Start"
            case "Next" => update(Event.Next); "Next"
            case "GameOver" => update(Event.GameOver); "GameOver"
            case "Cheat" => update(Event.Cheat); "Cheat"
            case "Help" => update(Event.Help); "Help"
            case "Input" => update(Event.Input); "Input"
            case "Load" => update(Event.Load); "Load"
            case "Save" => update(Event.Save); "Save"
            case "SaveTime" => update(Event.SaveTime); "SaveTime"
            case "Exit" => update(Event.Exit); "Exit"
            case _ => false; "wrong command"

        }
        println("TUI: executedCommand: " + executedCommand)
        val response: String = s"response : $executedCommand"
        Future.successful(response)
    }
}
