package de.htwg.se.minesweeper.controller.controllerComponent.controllerBaseImpl


import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpec

import de.htwg.se.minesweeper.model.gameComponent.gameBaseImpl._

import de.htwg.se.minesweeper.util.{Observer, Move}

import java.io.ByteArrayOutputStream
import de.htwg.se.minesweeper.util.Event
import de.htwg.se.minesweeper.Default.{given}
import de.htwg.se.minesweeper.controller.controllerComponent.IController
import de.htwg.se.minesweeper.model.gameComponent.IGame
import de.htwg.se.minesweeper.model.fileIoComponent.IFileIO
import de.htwg.se.minesweeper.Default

class ControllerSpec extends AnyWordSpec{



    "The Controller when created" should {
        val game1: IGame = Default.prepareGame(10, 9, 0)
        val startField = Default.createField(game1)

        val controller1 = new Controller(using game1)
        
        "have a game and field" in{
            controller1.game should be(game1)
        }

        "have no observers" in {
            controller1.subscribers should be (empty)
        }
    }

    
    "performing a first move" should {
        var game2 = Default.prepareGame(10, 9, 0)
        val emptyField2 = Default.createField(game2)

        val move2 = Move("open", 0 ,0)

        val controller2 = new Controller(using game2)
 
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
        var game = Default.prepareGame(10, 9, 0)
        val emptyField = Default.createField(game)

        val controller = new Controller(using game)
        val observer = new Observer {
            var notified = false
            override def update(e: Event): Boolean = 
                notified = true
                notified
        }
        controller.add(observer)

        "update the field" in {
            controller.flag(2, 2)
            controller.field.matrix.cell(2,2) should be (Symbols.F)
        }
    }

    "unflagging a field" should {
        var game = Default.prepareGame(10, 9, 0)
        val emptyField = Default.createField(game)

        val controller = new Controller(using game)
        val observer = new Observer {
        var notified = false
        override def update(e: Event): Boolean = 
            notified = true
            notified
        }
        controller.add(observer)

        "update the field" in {
            controller.unflag(2, 2)
            controller.field.matrix.cell(2,2) should be (Symbols.Covered)
      }

    }

    // TODO: refactor helMenue test

/*     "def helpMenue" should {
        var game = Default.prepareGame(10, 9, 0)
        val emptyField = Default.createField(game)

        val controller5 = new Controller(using game)
        val observer = new Observer {
        var notified = false
        override def update(e: Event): Boolean = 
            notified = true
            notified
        }
        controller5.add(observer)


        "def HelpMenue" in {

            controller5.helpMenu

            val testHelpMsg =
                """******************************************************************************
                  |* This is Minesweeper HELP - MENU                                            *
                  |* Enter your move (<action><x><y>, eg. o0002, q to quit, h for help):        *
                  |*                                                                            *
                  |* Possible moves: o0003 ----> opens cell on coordinates x=0, y=3             *
                  |*                 f0202 ----> flags cell on coordinates x=2, y=2             *
                  |*                 u0202 ----> removes flag at cell on coordinates x=2, y=2   *
                  |*                 z   ----> undo Move                                        *
                  |*                 y   ----> redo Move                                        *
                  |*                 r   ----> reaveals field                                   *
                  |*                 h   ----> help                                             *
                  |*                 q   ----> quits game                                       *
                  |*                                                                            *
                  |* Copyright: Steve Madoerin                                                  *
                  |*                                                                            *
                  |******************************************************************************""".stripMargin('|')
            val consoleOutput = new ByteArrayOutputStream()
            Console.withOut(consoleOutput){
                game.helpMessage
            }
            assert(consoleOutput.toString.trim == testHelpMsg.trim)
        }
    } */

    "cheat" should{
        val game4 = Game(3, 3, 0, "Playing")
        val newField = Default.createField(game4)
        val spielfeld = Playfield()
        val testfield = spielfeld.newField(3, game4)



        val controller7 = new Controller(using game4)
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

        var game22 = new Game(2, 3, 0, "Playing")
        val emptyField = new Field(3, Symbols.Covered)
        val invisible = emptyField.hidden.replaceCell(1, 1, Symbols.Bomb)	
        val testField = new Field(emptyField.matrix, invisible)
        val move3 = Move("open", 1 ,1)

        val controller1 = new Controller(using game22)

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
            resultField.showVisibleCell(2, 1) should not be (Symbols.Bomb)
        }
    }

    "the controllers put method" should {
        val game24: IGame = Default.prepareGame(10, 9, 0) // bombs, size, time
        val startField = Default.createField(game24)


        val controller = new Controller(using game24)
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
            controller.field.showVisibleCell(1, 1) should be (Symbols.Covered)
        }
    }

    "def makeAndPublish with 3 parameters" should {
        val game25: IGame = Default.prepareGame(10, 9, 0)
        val startField = Default.createField(game25)

        val controller8 = new Controller(using game25)
    
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
            controller8.field.showVisibleCell(1, 1) should not be (Symbols.Empty)
        }
    }

    "def makeAndPublish with 2 parameters" should {
        val game26 = new Game(3, 3, 0, "Playing")
        val emptyField = new Field(3, Symbols.Empty)


        val controller9 = new Controller(using game26)
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
            controller9.field.showVisibleCell(1, 1) should be (Symbols.F)
        }
    }

    "def gameOver" should {
        val game27 = new Game(3, 3, 0, "Playing")	
        val emptyField = new Field(3, Symbols.Empty)

        val controller10 = new Controller(using game27)
        val observer = new Observer {
        var notified = false
        override def update(e: Event): Boolean = 
            notified = true
            notified
        }
        controller10.add(observer)

        "make and publish a move" in {
            controller10.gameOver
            controller10.field should be (controller10.field.gameOverField)
        }
    }

    "def openRec" should {
        val game28: IGame = Default.prepareGame(10, 9, 0)
        val startField = Default.createField(game28)

        val controller11 = new Controller(using game28)
        val observer = new Observer {
        var notified = false
        override def update(e: Event): Boolean = 
            notified = true
            notified
        }
        controller11.add(observer)

        "make and publish a move" in {
            controller11.openRec(1, 1, startField)
            controller11.field.showVisibleCell(1, 1) should not be (Symbols.Empty)
        }
    }

    "def checkGameOver" should {
        val game29 = new Game(3, 3, 0, "Playing")
        val emptyField = new Field(3, Symbols.Empty)

        val controller12 = new Controller(using game29)
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
        val game31 = new Game(3, 3, 0, "Playing")
        val emptyField = new Field(3, Symbols.Empty)

        val controller14 = new Controller(using game31)
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
        val game32 = new Game(3, 3, 0, "Playing")
        val emptyField = new Field(3, Symbols.Empty)

        val controller15 = new Controller(using game32)
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
        val game34: IGame = Default.prepareGame(10, 9, 0)
        val startField = Default.createField(game34)

        val controller17 = new Controller(using game34)
        val observer = new Observer {
        var notified = false
        override def update(e: Event): Boolean = 
            notified = true
            notified
        }

        "make and publish a move" in {
            controller17.showVisibleCell(1, 1) should be ("~")
        }
    }


    "def newGameField" should{
        val game36 = new Game(3, 3, 0, "Playing")
        val emptyField = new Field(3, Symbols.Empty)

        val controller19 = new Controller(using game36)
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
        val game37: IGame = Default.prepareGame(10, 9, 0)
        val startField = Default.createField(game37)

        val controller20 = new Controller(using game37)
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
        val game38:IGame = new Game(3, 3, 0, "Playing")
        val startField38 = Default.createField(game38)

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

    // refactore saveScoreAndPlayerName

/*     "def saveScoreAndPlayerName" should{
        val game41 = new Game(3, 2, 0, "Playing")
        val emptyField = new Field(3, Symbols.Empty)

        val controller24 = new Controller(using game41)
        val observer = new Observer {
        var notified = false
        override def update(e: Event): Boolean =
            notified = true
            notified
        }

        "make and publish a move" in {
            val filePathHighScore = Default.filePathHighScore
            val score = 10
            controller24.saveScoreAndPlayerName("playerName", score, filePathHighScore)
        }
    } */

    // below refactor:

/*     "def loadPlayerScores" should{
        val game42 = new Game(3, 2, 0, "Playing")
        val emptyField = new Field(3, Symbols.Empty)

        val controller25 = new Controller(using game42)
        val observer = new Observer {
        var notified = false

        override def update(e: Event): Boolean =
            notified = true
            notified
        }

        "make and publish a move" in {
            val filePathHighScore = Default.filePathHighScore
            controller25.loadPlayerScores(filePathHighScore)
        }
    } */

/*     "def Controller.loadGame" should{
        val game43 = new Game(3, 2, 0, "Playing")
        val emptyField = new Field(3, Symbols.Empty)

        val controller26 = new Controller(using game43)
        val observer = new Observer {
        var notified = false

        override def update(e: Event): Boolean =
            notified = true
            notified
        }

        "make and publish a move" in {
            controller26.loadGame
        }
    } */

}