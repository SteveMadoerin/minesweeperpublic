package kafka

import io.circe.*
import io.circe.generic.auto.*
import io.circe.jawn.decode
import io.circe.syntax.*
import io.circe.{Decoder, Encoder}
import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.streams.kstream.{GlobalKTable, JoinWindows, TimeWindows, Windowed}
import org.apache.kafka.streams.scala.ImplicitConversions.*
import org.apache.kafka.streams.scala.*
import org.apache.kafka.streams.scala.kstream.{KGroupedStream, KStream, KTable}
import org.apache.kafka.streams.scala.serialization.Serdes
import org.apache.kafka.streams.scala.serialization.Serdes.*
import org.apache.kafka.streams.{KafkaStreams, StreamsConfig, Topology}
import org.apache.kafka.streams.scala.ImplicitConversions.*

import java.time.Duration
import java.time.temporal.ChronoUnit
import java.util.Properties

object ProducerGame extends App {

    import org.apache.kafka.clients.producer.*

    import java.util.Properties

    object Domain {
        type Bombs = Int
        type Side = Int
        type Time = Int
        type Board = String
        type GameId = String

        type Matrix = Vector[Vector[String]]
        type Hidden = Vector[Vector[String]]

        type Name = String
        type Score = Int

        type HighScores = Seq[(String, Int)]

        case class Game(bombs: Bombs, side: Side, time: Time, board: Board)

        case class Field(matrix: Matrix, hidden: Hidden)

        case class PlayerScore(playerName: Name, playerScore: Score)

        case class PlayerScores(highScoresList: HighScores)

    }

    object Topics {
        final val GameTopic = "game"
        final val FieldTopic = "field"
        final val PlayerScoreTopic = "player-score"
        final val PlayerScoresTopic = "player-scores"
    }


    import Domain._

    implicit def serde[A >: Null : Decoder : Encoder]: Serde[A] = {
        val serializer = (a: A) => a.asJson.noSpaces.getBytes
        val deserializer = (bytes: Array[Byte]) => {
            val string = new String(bytes)
            decode[A](string).toOption
        }
        Serdes.fromFn[A](serializer, deserializer)
    }

    def sendGameToTopic(game: Game, topic: String, gameId: String)(implicit producer: KafkaProducer[String, Array[Byte]]): Unit = {
        val gameSerde = serde[Game]
        val gameAsBytes = gameSerde.serializer().serialize(topic, game)
        val record = new ProducerRecord[String, Array[Byte]](topic, gameId, gameAsBytes)

        producer.send(record)
    }

    import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
    import org.apache.kafka.common.serialization.{StringSerializer, ByteArraySerializer}
    // Set up Kafka producer properties
    val props = new Properties()
    props.put("bootstrap.servers", "localhost:9092")
    props.put("key.serializer", classOf[StringSerializer].getName)
    props.put("value.serializer", classOf[ByteArraySerializer].getName)

    // Usage
    val game = Game(3, 3, 3, "Playing")
    val topic = "game"

    // Create a Kafka producer
    val producer: KafkaProducer[GameId, Array[Byte]] = new KafkaProducer[GameId, Array[Byte]](props)

    sendGameToTopic(game, topic, "1")(producer)
    producer.close()

}
