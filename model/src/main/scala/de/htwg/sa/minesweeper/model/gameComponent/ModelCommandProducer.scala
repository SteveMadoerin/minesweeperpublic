package de.htwg.sa.minesweeper.model.gameComponent

import akka.actor.ActorSystem
import akka.kafka.ProducerSettings
import akka.stream.scaladsl.Source
import org.apache.kafka.clients.producer.{ ProducerRecord, RecordMetadata}
import org.apache.kafka.common.serialization.StringSerializer
import akka.kafka.scaladsl.Producer
import akka.stream.Materializer
import akka.stream.scaladsl.Sink


import scala.concurrent.Future

import akka.kafka.ProducerMessage

class ModelCommandProducer(system: ActorSystem)(implicit val materializer: Materializer) {

    val ModelCommandTopic = "model-command"
    val ControllerComandTopic = "controller-command"

    // Configuration for the producer
    private val producerSettings = ProducerSettings(system, new StringSerializer, new StringSerializer)
        .withBootstrapServers("broker:29092")

    /*    private val producerSettings = ProducerSettings(system, new StringSerializer, new StringSerializer)
            .withBootstrapServers("localhost:9092")*/

    // Method to send a command and get back a Future[RecordMetadata]
    def sendCommand(command: String, moduleTopic: String): Future[RecordMetadata] = {
        val record = new ProducerRecord[String, String](moduleTopic, command)

        // Use Producer.flexiFlow to access the RecordMetadata
        val message = ProducerMessage.single(record)
        println(s"sending command: $command")
        Source.single(message)
            .via(Producer.flexiFlow(producerSettings))
            .map {
                case ProducerMessage.Result(metadata, _) => metadata
                // Handle other cases such as passThrough and multiResult if needed
            }
            .runWith(Sink.head)(akka.stream.Materializer.matFromSystem(system))
    }
}