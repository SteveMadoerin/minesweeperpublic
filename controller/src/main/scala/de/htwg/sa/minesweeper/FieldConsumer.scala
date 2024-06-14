package de.htwg.sa.minesweeper

import akka.actor.ActorSystem
import akka.kafka.scaladsl.Consumer
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.stream.Materializer
import akka.stream.Materializer.matFromSystem
import akka.stream.scaladsl.Sink
import de.htwg.sa.minesweeper.entity.{FieldDTO, GameDTO, MatrixDTO}
import de.htwg.sa.minesweeper.util.RestUtil
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.{StringDeserializer, StringSerializer}

import scala.concurrent.{ExecutionContext, Future}

class FieldConsumer(system: ActorSystem)(implicit val materializer: Materializer) {
    val ControllerFieldTopic = "controller-field"

    private val consumerSettings = ConsumerSettings(system, new StringDeserializer, new StringDeserializer)
        .withBootstrapServers("broker:29092")
        .withGroupId("test-group")
        .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
        //.withBootstrapServers("broker:9092")

    // Method to start consuming
    def startConsuming()(implicit ec: ExecutionContext): Future[FieldDTO] = {
        Consumer.plainSource(consumerSettings, Subscriptions.topics(ControllerFieldTopic))
            .mapAsync(1)(record => processCommand(record.value()))
            .takeWhile(_.isRight, inclusive = true) // Only take elements until a valid field is received
            .runWith(Sink.head) // Take the first element
            .flatMap {
                case Right(field) => Future.successful(field)
                case Left(error) => Future.failed(new Exception(error))
            }
    }
/*    def startConsuming(): Unit = {
        Consumer.plainSource(consumerSettings, Subscriptions.topics(ControllerFieldTopic))
            .mapAsync(1)(record => processCommand(record.value()))
            .runWith(Sink.ignore)(Materializer.matFromSystem(system))
    }*/

    private def processCommand(field: String): Future[Either[String, FieldDTO]] = {
        field match {
            case null =>
                val error = "Error fetching field: "
                println(error)
                Future.successful(Left(error))
            case _ =>
                println("Successfully fetched field: ")
                val leField = RestUtil.jsontToFieldDTO(field)
                println("field: " + leField)
                Future.successful(Right(leField))
        }
    }

    // Method to process the command
/*    private def processCommand(field: String): Future[FieldDTO] = {
        val leField = field match {
            case null => {
                println("Error fetching field: ")
                FieldDTO(MatrixDTO[String](Vector(Vector.empty)), MatrixDTO[String](Vector(Vector.empty)))
            }
            case _ => {
                println("Successfully fetched field: ")
                RestUtil.jsontToFieldDTO(field)
            }
        }
        println("field: " + leField)
        
        Future.successful(leField)
    }*/
}
