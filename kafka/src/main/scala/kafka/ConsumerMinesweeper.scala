package kafka

import io.circe.generic.auto.*
import io.circe.jawn.decode
import io.circe.syntax.*
import io.circe.*
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.streams.kstream.{GlobalKTable, JoinWindows, TimeWindows, Windowed}
import org.apache.kafka.streams.scala.*
import org.apache.kafka.streams.scala.ImplicitConversions.*
import org.apache.kafka.streams.scala.kstream.{KGroupedStream, KStream, KTable}
import org.apache.kafka.streams.scala.serialization.Serdes
import org.apache.kafka.streams.scala.serialization.Serdes.*
import org.apache.kafka.streams.{KafkaStreams, StreamsConfig, Topology}

import java.time.Duration
import java.time.temporal.ChronoUnit
import java.util
import java.util.Properties
import scala.collection.JavaConverters.*

object ConsumerMinesweeper extends App {

    object Domain {
        type Bombs = Int
        type Side = Int
        type Time = Int
        type Board = String

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

    // source  = emits elements
    // flows = transforms elements along the way (e.g. map)
    // sink = "ingests" elements

    import Domain.*

    implicit def serde[A >: Null : Decoder : Encoder]: Serde[A] = {
        val serializer = (a: A) => a.asJson.noSpaces.getBytes
        val deserializer = (bytes: Array[Byte]) => {
            val string = new String(bytes)
            decode[A](string).toOption
        }
        Serdes.fromFn[A](serializer, deserializer)
    }

    import java.util.Properties

    val TOPIC="game"

    val  props = new Properties()
    props.put("bootstrap.servers", "localhost:9092")

    props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
    props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
    props.put("group.id", "something")

    val consumer = new KafkaConsumer[String, Game](props)

    consumer.subscribe(util.Collections.singletonList(TOPIC))

    while(true){
        val records=consumer.poll(100)
        for (record<-records.asScala){
            println(record)
            println(record.key())
            println(record.value())
/*            val game: Game = record.value()
            println(game.board)*/
        }
    }
}
