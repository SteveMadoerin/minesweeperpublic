/*
package de.htwg.sa.minesweeper.model.gameComponent.util

import org.apache.kafka.clients.consumer.{ConsumerConfig, KafkaConsumer}
import java.time.Duration
import java.util.Properties
import java.util.Collections
import io.circe.generic.auto._
import io.circe.parser._
import org.apache.kafka.common.serialization.StringDeserializer

object KafkaGameConsumer extends App {

    def consumeGame: Game = {
        val topic = "game"
        val props = new Properties()
        props.put("bootstrap.servers", "localhost:9092")
        props.put("group.id", "test-group")
        props.put("key.deserializer", classOf[StringDeserializer].getName)
        props.put("value.deserializer", classOf[StringDeserializer].getName)
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

        val consumer = new KafkaConsumer[String, String](props)
        val currentGame = Game(0, 0, 0, "")
        try {
            //for each topics get record value
            consumer.subscribe(Collections.singletonList(topic))

            val records = consumer.poll(Duration.ofMillis(100))
            records.forEach { record =>
                decode[Game](record.value()) match {
                    case Right(game) => println(s"Received game: $game"); game
                    case Left(error) => println(s"Failed to deserialize game: $error"); currentGame
                }
            }
            currentGame

        } finally {
            consumer.close()
        }
    }

    def pollingGameInfinit = {
        val topic = "game"
        //weitere topics
        val props = new Properties()
        props.put("bootstrap.servers", "localhost:9092")
        props.put("group.id", "test-group")
        props.put("key.deserializer", classOf[StringDeserializer].getName)
        props.put("value.deserializer", classOf[StringDeserializer].getName)
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

        val consumer = new KafkaConsumer[String, String](props)

        try {
            //for each topics get record value
            consumer.subscribe(Collections.singletonList(topic))
            while (true) {
                val records = consumer.poll(Duration.ofMillis(100))
                records.forEach { record =>
                    decode[Game](record.value()) match {
                        case Right(game) => println(s"Received game: $game")
                        case Left(error) => println(s"Failed to deserialize game: $error")
                    }
                }
            }
        } finally {
            consumer.close()
        }
    }

    val testGame = consumeGame
    println(testGame)


}

case class Game(bombs: Int, side: Int, time: Int, board: String)
*/
