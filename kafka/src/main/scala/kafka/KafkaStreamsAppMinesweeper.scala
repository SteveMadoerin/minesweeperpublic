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

object KafkaStreamsAppMinesweeper {

    object Domain{
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

    object Topics{
        final val GameTopic = "game"
        final val FieldTopic = "field"
        final val PlayerScoreTopic = "player-score"
        final val PlayerScoresTopic = "player-scores"
    }

    // source  = emits elements
    // flows = transforms elements along the way (e.g. map)
    // sink = "ingests" elements

    import Domain._

    implicit def serde[A >: Null : Decoder : Encoder]: Serde[A] = {
        val serializer = (a: A) => a.asJson.noSpaces.getBytes
        val deserializer = (bytes: Array[Byte]) => {
            val string = new String(bytes)
            decode[A](string).toOption
        }
        Serdes.fromFn[A](serializer, deserializer)
    }
    
    

    def main(args: Array[String]): Unit = {
        // after starting docker compose go to git bash and give in: docker exec -it broker bash
        // then run def main and copy the output and put it in the terminal
        List (
            "game",
            "field",
            "player-score",
            "player-scores"
        ).foreach { topic =>
            println(s"kafka-topics --bootstrap-server localhost:9092 --topic ${topic} --create")
        }
    }
}
