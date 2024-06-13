package de.htwg.sa.minesweeper.model.gameComponent

import akka.actor.ActorSystem
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.kafka.scaladsl.Consumer
import akka.stream.Materializer
import akka.stream.scaladsl.Sink
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer

import scala.concurrent.Future
import akka.stream.Materializer.matFromSystem
import de.htwg.sa.minesweeper.model.gameComponent.gameBaseImpl.{Game, Playfield}

class ModelCommandConsumer(system: ActorSystem)(implicit val materializer: Materializer) {

    val CommandsTopic = "model-command"

    private val consumerSettings = ConsumerSettings(system, new StringDeserializer, new StringDeserializer)
        .withBootstrapServers("localhost:9092")
        .withGroupId("test-group")
        .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
    
    def startConsuming(): Unit = {
        Consumer.plainSource(consumerSettings, Subscriptions.topics(CommandsTopic))
            .mapAsync(1)(record => processCommand(record.value()))
            .runWith(Sink.ignore)(Materializer.matFromSystem(system))
    }

    def createField(leGame: IGame): IField = {
        val adjacentField = Playfield()
        val tempGame: Game = leGame.asInstanceOf[Game]
        adjacentField.newField(leGame.side, tempGame)
    }
    
    private def processCommand(command: String): Future[Unit] = {
        val executedCommand = command match {
            case "/model/game" => {
                val game = Game(10, 9, 0, "Playing")
                println("HttpResponse(entity = game.gameToJson.toString())")
                command
                //gameService.startGame()
            }
            case command if command.startsWith("/model/field/new") =>
                val commandString = command
                val params = commandString.split("&")
                val bombs = params(0).split("=")(1).toInt
                val size = params(1).split("=")(1).toInt
                val time = params(2).split("=")(1).toInt
                val spiel = Game(bombs, size, time, "Playing")
                val feld = createField(spiel)
                val fieldProducer = new ModelResponseProducer(system)
                fieldProducer.sendResponse(feld.fieldToJson, fieldProducer.ControllerFieldTopic)
                println("model produces field: Start")
                println(feld.fieldToJson)
                println("model produces field: End")
            case _ => {
                println("executedCommand not found: " + command)
                command
                // Handle other commands if needed
            }   
        }
        println("executedCommand: " + executedCommand)
        val response: String = s"response: $executedCommand"
        //val game = gameService.startNewGame(startGameCommand.bombs, startGameCommand.size, startGameCommand.time)
        // After processing, you can produce a response to a response topic
        // TODO: uncomment to send a response
/*        val gameResponseProducer = new ModelResponseProducer(system)
        gameResponseProducer.sendResponse(response)*/
        Future.successful(())
    }
}
