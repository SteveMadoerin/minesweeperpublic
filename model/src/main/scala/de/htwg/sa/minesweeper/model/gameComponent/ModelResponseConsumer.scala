package de.htwg.sa.minesweeper.model.gameComponent.ModelResponseConsumer

import akka.actor.ActorSystem
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.kafka.scaladsl.Consumer
import akka.stream.Materializer
import akka.stream.scaladsl.Sink
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer

import scala.concurrent.Future
import akka.stream.Materializer.matFromSystem
import de.htwg.sa.minesweeper.model.gameComponent.ModelResponseProducer

class ModelResponseConsumer(system: ActorSystem) {

    val ResponseTopic = "model-response"

    private val consumerSettings = ConsumerSettings(system, new StringDeserializer, new StringDeserializer)
        .withBootstrapServers("localhost:9092")
        .withGroupId("test-group")
        .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
    
    def startConsuming(): Unit = {
        Consumer.plainSource(consumerSettings, Subscriptions.topics(ResponseTopic))
            .mapAsync(1)(record => processResponse(record.value()))
            .runWith(Sink.ignore)(Materializer.matFromSystem(system))
    }
    
    private def processResponse(response: String): Future[Unit] = {
        println("Model received response: " + response)
        //val game = gameService.startNewGame(startGameCommand.bombs, startGameCommand.size, startGameCommand.time)
        // After processing, you can produce a response to a response topic
/*        val gameResponseProducer = new ModelResponseProducer(system)
        gameResponseProducer.sendResponse(response)*/

        Future.successful(())
    }
}
