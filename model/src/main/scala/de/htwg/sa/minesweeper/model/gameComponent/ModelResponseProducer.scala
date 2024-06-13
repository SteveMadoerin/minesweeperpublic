package de.htwg.sa.minesweeper.model.gameComponent

import akka.actor.ActorSystem
import akka.actor.ActorSystem
import akka.kafka.ProducerSettings
import akka.stream.scaladsl.Source
import org.apache.kafka.clients.producer.{ProducerRecord, RecordMetadata}
import org.apache.kafka.common.serialization.StringSerializer
import akka.kafka.scaladsl.Producer
import akka.stream.Materializer
import akka.stream.scaladsl.Sink

import scala.concurrent.Future
import akka.kafka.ProducerMessage

class ModelResponseProducer(system: ActorSystem) {

    val ControllerResponseTopic = "controller-response"
    val ModelResponseTopic = "model-response"
    val ControllerFieldTopic = "controller-field"
    
    private val producerSettings = ProducerSettings(system, new StringSerializer, new StringSerializer)
        .withBootstrapServers("localhost:9092")
    
    def sendResponseRecord(response: String, moduleTopic: String): Future[RecordMetadata] = {
        val record = new ProducerRecord[String, String](moduleTopic, response)
        
        val message = ProducerMessage.single(record)
        Source.single(message)
            .via(Producer.flexiFlow(producerSettings))
            .map {
                case ProducerMessage.Result(metadata, _) => metadata
                // Handle other cases such as passThrough and multiResult if needed
            }
            .runWith(Sink.head)(akka.stream.Materializer.matFromSystem(system))
    }

    def sendResponse(response: String, moduleTopic: String): Future[String] = {
        val record = new ProducerRecord[String, String](moduleTopic, response)
        val message = ProducerMessage.single(record)

        Source.single(message)
            .via(Producer.flexiFlow(producerSettings))
            .map {
                case ProducerMessage.Result(metadata, _) => metadata.toString
                // Handle other cases such as passThrough and multiResult if needed
            }
            .runWith(Sink.head)(akka.stream.Materializer.matFromSystem(system))
    }
}
