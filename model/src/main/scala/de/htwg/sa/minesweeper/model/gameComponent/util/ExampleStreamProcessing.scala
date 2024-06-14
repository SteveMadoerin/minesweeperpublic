/*
package de.htwg.sa.minesweeper.model.gameComponent.util

import org.apache.kafka.streams.{KafkaStreams, StreamsBuilder, StreamsConfig}
import org.apache.kafka.streams.kstream.{KStream, Produced}

import java.util.Properties
import org.apache.kafka.streams.scala.ImplicitConversions.*
import org.apache.kafka.streams.scala.Serdes
import org.apache.kafka.streams.scala.kstream.Consumed

import scala.io.StdIn.readLine

object ExampleStreamProcessing extends App {
    // Sets up the necessary properties for the Kafka Streams application.
    val props: Properties = {
        val p = new Properties()
        p.put(StreamsConfig.APPLICATION_ID_CONFIG, "kafka-streams-example")
        p.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
        p.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String.getClass.getName)
        p.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String.getClass.getName)
        p
    }

    val builder: StreamsBuilder = new StreamsBuilder() // Defines a StreamsBuilder to build the processing topology.
    // Implicitly converts Serdes to the correct serdes needed for the stream method
    implicit val consumed: Consumed[String, String] = Consumed.`with`(Serdes.String, Serdes.String)
    val textLines: KStream[String, String] = builder.stream("input-topic") // Creates a KStream that reads from input-topic.
    val uppercasedLines: KStream[String, String] = textLines.mapValues(_.toUpperCase()) // Uses mapValues to convert each message to uppercase.

    // Implicitly converts Serdes to the correct serdes needed for the to method
    implicit val produced: Produced[String, String] = Produced.`with`(Serdes.String, Serdes.String)
    uppercasedLines.to("output-topic") // Writes the processed messages to output-topic.

    val streams: KafkaStreams = new KafkaStreams(builder.build(), props)
    streams.start()

    sys.ShutdownHookThread {
        streams.close()
    }
}

import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord, ProducerConfig}
import org.apache.kafka.common.serialization.StringSerializer

object SimpleKafkaProducer extends App {
    val topic = "input-topic"
    val props = new Properties()

    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, classOf[StringSerializer].getName)
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, classOf[StringSerializer].getName)

    val producer = new KafkaProducer[String, String](props)

    // Example message
    val message = readLine()

    try {
        // Send a message to the topic
        val record = new ProducerRecord[String, String](topic, "key", message)
        producer.send(record)
        println(s"Sent message to $topic: $message")
    } catch {
        case e: Exception => e.printStackTrace()
    } finally {
        producer.close()
    }
}


import java.time.Duration
import java.util.{Collections, Properties}
import org.apache.kafka.clients.consumer.{ConsumerConfig, KafkaConsumer}
import org.apache.kafka.common.serialization.StringDeserializer
import scala.collection.JavaConverters._

object SimpleKafkaConsumer extends App {
    val topic = "output-topic"
    val props = new Properties()

    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, classOf[StringDeserializer].getName)
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, classOf[StringDeserializer].getName)
    props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-group")
    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

    val consumer = new KafkaConsumer[String, String](props)

    try {
        consumer.subscribe(Collections.singletonList(topic))

        while (true) {
            val records = consumer.poll(Duration.ofMillis(100))
            for (record <- records.asScala) {
                println(s"Received message: (${record.key()}, ${record.value()}) at offset ${record.offset()}")
            }
        }
    } catch {
        case e: Exception => e.printStackTrace()
    } finally {
        consumer.close()
    }
}
*/
