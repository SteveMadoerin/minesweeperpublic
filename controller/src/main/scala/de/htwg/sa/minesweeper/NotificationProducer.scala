package de.htwg.sa.minesweeper

import akka.actor.ActorSystem
import akka.kafka.scaladsl.Producer
import akka.kafka.{ProducerMessage, ProducerSettings}
import akka.stream.Materializer
import akka.stream.scaladsl.{Sink, Source}
import org.apache.kafka.clients.producer.{ProducerRecord, RecordMetadata}
import org.apache.kafka.common.serialization.StringSerializer

import scala.concurrent.Future

class NotificationProducer(system: ActorSystem)(implicit val materializer: Materializer) {

    val TuiCommandTopic = "tui-notify"
    val GuiCommandTopic = "gui-notify"
    
    private val producerSettings = ProducerSettings(system, new StringSerializer, new StringSerializer)
        .withBootstrapServers("broker:29092")
    
    def sendCommand(command: String, moduleTopic: String): Future[RecordMetadata] = {
        val record = new ProducerRecord[String, String](moduleTopic, command)
        
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