package kafka

import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import java.util.Properties
import io.circe.generic.auto._
import io.circe.syntax._
import org.apache.kafka.common.serialization.StringSerializer

object KafkaGameProducer extends App {

    case class Game(bombs: Int, side: Int, time: Int, board: String)
    val topic = "game"
    val game = Game(10, 10, 10, "Playing")

    val props = new Properties()
    props.put("bootstrap.servers", "localhost:9092")
    props.put("key.serializer", classOf[StringSerializer].getName)
    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")

    val producer = new KafkaProducer[String, String](props)

    try {
        val gameAsJson = game.asJson.noSpaces
        val record = new ProducerRecord[String, String](topic, gameAsJson)
        producer.send(record)
        println(s"Sent game to topic $topic: $gameAsJson")
    } finally {
        producer.close()
    }
}


