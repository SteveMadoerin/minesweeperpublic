package de.htwg.sa.minesweeper

import akka.actor.ActorSystem
import akka.kafka.scaladsl.Consumer
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.stream.Materializer
import akka.stream.Materializer.matFromSystem
import akka.stream.scaladsl.Sink
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer

import scala.concurrent.Future

class ControllerResponseConsumer(system: ActorSystem) {

    val ControllerResponseTopic = "controller-response"

    private val consumerSettings = ConsumerSettings(system, new StringDeserializer, new StringDeserializer)
        .withBootstrapServers("localhost:9092")
        .withGroupId("test-group")
        .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
    
    def startConsuming(): Unit = {
        Consumer.plainSource(consumerSettings, Subscriptions.topics(ControllerResponseTopic))
            .mapAsync(1)(record => processResponse(record.value()))
            .runWith(Sink.ignore)(Materializer.matFromSystem(system))
    }
    
    private def processResponse(response: String): Future[Unit] = {

        println("controller has received response: " + response)
        // maybe do something
        Future.successful(())
    }
}
