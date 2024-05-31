package de.htwg.sa.minesweeper.controller.controllerComponent.controllerBaseImpl


import org.scalatest.matchers.should.Matchers.*
import org.scalatest.wordspec.AnyWordSpec

import java.io.ByteArrayOutputStream
import de.htwg.sa.minesweeper.util.{Event, Move, Observer}
import de.htwg.sa.minesweeper.controller.controllerComponent.config.Default.given
import de.htwg.sa.minesweeper.controller.controllerComponent.IController
import de.htwg.sa.minesweeper.controller.controllerComponent.config.Default
import de.htwg.sa.minesweeper.controller.controllerComponent.controllerBaseImpl.Controller
import de.htwg.sa.minesweeper.entity.{FieldDTO, GameDTO, MatrixDTO}


class ControllerSpec extends AnyWordSpec{

    val initController = new Controller
    val initGame = initController.game
    val initField = initController.createFieldDTO(initGame)



    "The Controller when created" should {
        val game1 = GameDTO(10, 9, 0, "Playing")
        val controller1 = Controller()
        
        "have a game and field" in{
            controller1.game should be(game1)
        }

        "have no observers" in {
            controller1.subscribers should be (empty)
        }
    }

    
    "performing a first move" should {
        var game2 = GameDTO(10, 9, 0, "Playing")
        val move2 = Move("open", 0 ,0)

        val controller2 = new Controller()

        val observer = new Observer {
            var notified = false
            override def update(e: Event): Boolean =
                notified = true
                notified
        }
        controller2.add(observer)

        "update the field after firstMove" in {
            controller2.doMove(true, move2, game2)
            game2.board should be ("Playing")
        }

    }

    "flagging a field" should {
        var game = GameDTO(10, 9, 0, "Playing")

        val controller = new Controller()
        val observer = new Observer {
            var notified = false
            override def update(e: Event): Boolean = 
                notified = true
                notified
        }
        controller.add(observer)

        "update the field" in {

            controller.field.matrix.rows(2).updated(2, "F") should be ("F")
        }
    }

    "unflagging a field" should {
        var game = new GameDTO(10, 9, 0, "Playing")

        val controller = new Controller()
        val observer = new Observer {
        var notified = false
        override def update(e: Event): Boolean = 
            notified = true
            notified
        }
        controller.add(observer)

        "update the field" in {
            controller.field.matrix.rows(2).updated(2, "~") should be ("~")
      }

    }

    "def helpMenue" should {
        val controller5 = new Controller()
        val observer = new Observer {
        var notified = false
        override def update(e: Event): Boolean = 
            notified = true
            notified
        }
        controller5.add(observer)

        "print the helpMenue" in {
            controller5.helpMenu
        }

    }

    "cheat" should{
        val game4 = GameDTO(3, 3, 0, "Playing")
        val controller7 = new Controller()
        val observer = new Observer {
        var notified = false
        override def update(e: Event): Boolean = 
            notified = true
            notified
        }
        controller7.add(observer)

        "reveal all fields" in {
            controller7.cheat

        }

    }

    "uncover field" should {

        var game22 = GameDTO(2, 3, 0, "Playing")
        val emptyField = FieldDTO(MatrixDTO(Vector.fill(3)(Vector.fill(3)("~"))), MatrixDTO(Vector.fill(3)(Vector.fill(3)("~"))))
        val invisible = MatrixDTO(emptyField.hidden.rows.updated(1, emptyField.hidden.rows(1).updated(1, "F"))) // replaceCell(1, 1, "*")
        val testField = FieldDTO(emptyField.matrix, invisible)
        val move3 = Move("open", 1 ,1)

        val controller1 = new Controller()

        var notified = false
        val observer = new Observer {
        override def update(e: Event): Boolean = 
            notified = true
            notified
        }
        controller1.add(observer)

        "Uncover Field Function" in {
        
            controller1.doMove(false, move3, game22)
            val resultField = controller1.field
            resultField.matrix.rows(2)(1) should not be ("*")
        }
    }

    "the controllers put method" should {
        val game24= GameDTO(10, 9, 0, "Playing")

        val controller = new Controller()
        val observer = new Observer {
        var notified = false
        override def update(e: Event): Boolean = 
            notified = true
            notified
        }
        controller.add(observer)

        "put a move" in {
            val move = Move("open", 1, 1)
            controller.put(move)
            controller.field.matrix.rows(1)(1) should be ("~")
        }
    }

    "def makeAndPublish with 3 parameters" should {
        val game25 = GameDTO(10, 9, 0, "Playing")

        val controller8 = new Controller()
    
        val observer = new Observer {
        var notified = false
        override def update(e: Event): Boolean = 
            notified = true
            notified
        }
        controller8.add(observer)

        "make and publish a move" in {
            val move = Move("open", 1, 1)
            controller8.makeAndPublish(controller8.doMove, false, move, game25)
            controller8.field.matrix.rows(1)(1) should not be (" ")
        }
    }

    "def makeAndPublish with 2 parameters" should {
        val game26 = GameDTO(3, 3, 0, "Playing")
        val emptyField =  FieldDTO(MatrixDTO(Vector.fill(3)(Vector.fill(3)(" "))), MatrixDTO(Vector.fill(3)(Vector.fill(3)(" ")))) /*Field(3, " ")*/


        val controller9 = new Controller()
        val observer = new Observer {
        var notified = false
        override def update(e: Event): Boolean = 
            notified = true
            notified
        }
        controller9.add(observer)

        "make and publish a move" in {
            val move = Move("flag", 1, 1)
            controller9.makeAndPublish(controller9.put, move)
            controller9.field.matrix.rows(1)(1) should be ("F")
        }
    }

    "def gameOver" should {
        val game27 = GameDTO(3, 3, 0, "Playing")
        val emptyField = FieldDTO(MatrixDTO(Vector.fill(3)(Vector.fill(3)(" "))), MatrixDTO(Vector.fill(3)(Vector.fill(3)(" ")))) /*new Field(3, " ")*/

        val controller10 = new Controller()
        val observer = new Observer {
        var notified = false
        override def update(e: Event): Boolean = 
            notified = true
            notified
        }
        controller10.add(observer)

        "make and publish a move" in {
            controller10.gameOver
            controller10.field should be (FieldDTO(controller10.field.hidden, controller10.field.hidden))
        }
    }

    "def openRec" should {
        val game28 = GameDTO(10, 9, 0, "Playing")
        val startField = initController.createFieldDTO(game28)

        val controller11 = new Controller()
        val observer = new Observer {
        var notified = false
        override def update(e: Event): Boolean = 
            notified = true
            notified
        }
        controller11.add(observer)

        "make and publish a move" in {
            controller11.openRec(1, 1, startField)
            controller11.field.matrix.rows(1)(1) should not be (" ")
        }
    }

    "def checkGameOver" should {
        val game29 = GameDTO(3, 3, 0, "Playing")
        val emptyField =  FieldDTO(MatrixDTO(Vector.fill(3)(Vector.fill(3)(" "))), MatrixDTO(Vector.fill(3)(Vector.fill(3)(" ")))) //Field(3, " ")

        val controller12 = new Controller()
        val observer = new Observer {
        var notified = false
        override def update(e: Event): Boolean = 
            notified = true
            notified
        }
        controller12.add(observer)

        "make and publish a move" in {
            controller12.checkGameOver
        }
    }

    "def newGameGui" should {
        val game31 = GameDTO(3, 3, 0, "Playing")
        val emptyField =  FieldDTO(MatrixDTO(Vector.fill(3)(Vector.fill(3)(" "))), MatrixDTO(Vector.fill(3)(Vector.fill(3)(" ")))) //Field(3, " ")

        val controller14 = new Controller()
        val observer = new Observer {
        var notified = false
        override def update(e: Event): Boolean = 
            notified = true
            notified
        }

        "Trigger input in Gui" in {
            controller14.newGameGUI
        }
    }

    "def newGame" should {
        val game32 = GameDTO(3, 3, 0, "Playing")
        val emptyField =  FieldDTO(MatrixDTO(Vector.fill(3)(Vector.fill(3)(" "))), MatrixDTO(Vector.fill(3)(Vector.fill(3)(" ")))) //new Field(3, " ")

        val controller15 = new Controller()
        val observer = new Observer {
        var notified = false
        override def update(e: Event): Boolean = 
            notified = true
            notified
        }

        "make and publish a move" in {
            val in = new java.io.ByteArrayInputStream("steve\n0\n".getBytes)
                Console.withIn(in){
                    controller15.newGame(5 , 5)
                    controller15.notifyObservers(Event.NewGame)
                }
            
        }
    }

    "def showVisibleCell" should {
        val game34 = new GameDTO(10, 9, 0, "Playing")
        val startField = initController.createFieldDTO(game34)

        val controller17 = new Controller()
        val observer = new Observer {
        var notified = false
        override def update(e: Event): Boolean = 
            notified = true
            notified
        }

        "make and publish a move" in {
            controller17.field.matrix.rows(1)(1) should be ("~")
        }
    }


    "def newGameField" should{
        val game36 = GameDTO(3, 3, 0, "Playing")
        val emptyField =  FieldDTO(MatrixDTO(Vector.fill(3)(Vector.fill(3)(" "))), MatrixDTO(Vector.fill(3)(Vector.fill(3)(" ")))) //new Field(3, " ")

        val controller19 = new Controller()
        val observer = new Observer {
        var notified = false
        override def update(e: Event): Boolean = 
            notified = true
            notified
        }

        "make and publish a move" in {
            controller19.newGameField(Some("Easy"))
            controller19.notifyObservers(Event.NewGame)
        }
    }

    "def board" should {
        val game37 = GameDTO(10, 9, 0, "Playing")
        val startField = initController.createFieldDTO(game37)

        val controller20 = Controller()
        val observer = new Observer {
        var notified = false
        override def update(e: Event): Boolean = 
            notified = true
            notified
        }

        "make and publish a move" in {
            controller20.game.board
        }
    }

    "def field" should {
        val game38 = GameDTO(3, 3, 0, "Playing")
        val startField38 = initController.createFieldDTO(game38)

        val controller21 = new Controller()
        val observer = new Observer {
        var notified = false
        override def update(e: Event): Boolean = 
            notified = true
            notified
        }

        "make and publish a move" in {
            controller21.field should not be (startField38)
        }

    }

    "def controllerSaveGame" should{

        val controller22 = new Controller()
        val observer = new Observer {
        var notified = false
        override def update(e: Event): Boolean = 
            notified = true
            notified
        }

        "make and publish a move" in {
            controller22.saveGame
        }
    }

    "def saveScoreAndPlayerName" should{
        val game41 = GameDTO( 10, 9, 0, "Playing")

        val controller24 = new Controller()
        val observer = new Observer {
        var notified = false
        override def update(e: Event): Boolean =
            notified = true
            notified
        }

        "make and publish a move" in {
            val filePathHighScore = ""
            val score = 10
            controller24.saveScoreAndPlayerName("playerName", score, filePathHighScore)
        }
    }

    // sometimes this test could fail because of concurrency
    "def loadPlayerScores" should{
        val game42 = GameDTO( 10, 9, 0, "Playing")

        val controller25 = new Controller()
        val observer = new Observer {
        var notified = false

        override def update(e: Event): Boolean =
            notified = true
            notified
        }

        "make and publish a move" in {
/*             val filePathHighScore = Default.filePathHighScore
            controller25.loadPlayerScores(filePathHighScore) */
            controller25.loadPlayerScores
        }
    }

    "def Controller.loadGame" should{
        val game43 = GameDTO( 10, 9, 0, "Playing")

        val controller26 = new Controller()
        val observer = new Observer {
        var notified = false

        override def update(e: Event): Boolean =
            notified = true
            notified
        }

        "make and publish a move" in {
            controller26.loadGame
        }
    }

}