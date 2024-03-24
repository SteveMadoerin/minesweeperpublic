package de.htwg.se.minesweeper.model.gameComponent.gameBaseImpl

import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpec
import java.io.ByteArrayOutputStream


class GameSpec extends AnyWordSpec {
    
/*     "The chooseDifficulty method" should {

        val newGame = new Game(Status.Playing)

        "return the super easy difficulty when input is 0" in {
            val testGame = newGame
            val in = new java.io.ByteArrayInputStream("0\n".getBytes)
            Console.withIn(in){
                testGame.setSideAndBombs(5, 5)
                testGame.getSide should be (5)
                testGame.getBombs should be (5)
            }
        }
        
        "return the beginner difficulty when input is 1" in {
            val testGame = new Game(Status.Playing)
            val in = new java.io.ByteArrayInputStream("1\n".getBytes)
            Console.withIn(in){
                testGame.setSideAndBombs(9, 10)
                testGame.getSide should be (9)
                testGame.getBombs should be (10)
            }
        }

        "return the intermediate difficulty when input is 2" in {
            val testGame = new Game(Status.Playing)
            val in = new java.io.ByteArrayInputStream("2\n".getBytes)
            Console.withIn(in){
                testGame.setSideAndBombs(15, 40)
                testGame.getSide should be (15)
                testGame.getBombs should be (40)
            }
        }

        "return the advanced difficulty when input is 3" in {
            val testGame = new Game(Status.Playing)
            val in = new java.io.ByteArrayInputStream("3\n".getBytes)
            Console.withIn(in) {
                testGame.setSideAndBombs(19, 85)
                testGame.getSide should be (19)
                testGame.getBombs should be (85)
            }
        }

        "return the beginner difficulty by default when input is not recognized" in{
            val testGame = new Game(Status.Playing)
            val in = new java.io.ByteArrayInputStream("33\n".getBytes)
            Console.withIn(in){
                testGame.setSideAndBombs(9, 10)
                testGame.getSide should be (9)
                testGame.getBombs should be (10)
            }
        }
    }
 */

    "Method replaceBomb" should {
        val testGame10 = new Game(Status.Playing, 2, 3)

        val sicht1 = new Matrix(4, Symbols.Covered)
        val unsicht1 = new Matrix(4, Symbols.Empty)
        val unsicht2 = unsicht1.replaceCell(1, 1, Symbols.Bomb)
        val testField1 = new Field(sicht1, unsicht2)


        "replace matrix and ensure the player doesn't click on bomb at first move" in{

            val result33 = testGame10.replaceBomb(1, 1, testField1)
            result33.showInvisibleCell(1, 1) should not be (Symbols.Bomb)
        }

    }

    "Premier move" should{

        val game42 = new Game(Status.Playing, 1, 3)

        val sicht11 = new Matrix(4, Symbols.Covered)
        val unsicht11 = new Matrix(4, Symbols.Empty)
        val unsicht22 = unsicht11.replaceCell(1, 1, Symbols.Bomb)
        val preparedField = new Field(sicht11, unsicht22)

        "guarante that you do not loose on first move" in{
            val result42 = game42.premierMove(1, 1, preparedField)
            result42.showInvisibleCell(1, 1) should not be (Symbols.Bomb)
        }
    }

    "Method check exit" should {
        "return false game is in Status playing" in {
            var testGame55 = new Game(Status.Playing, 1, 3)	
            val testB = "Playing"

            testGame55.handleGameState(testB)

            testGame55.checkExit should be (false)


        }
    }
    
    "def calcMineAndFlag" should {
        val testGame = new Game(Status.Playing , 1, 3)

        val sicht = new Matrix(4, Symbols.Covered)
        val unsicht = new Matrix(4, Symbols.Zero)
        val unsicht2 = unsicht.replaceCell(1, 1, Symbols.Bomb)
        val testField = new Field(sicht, unsicht2)

        "return the number of flags minus bombs" in {
            val result = testGame.calcMineAndFlag(testField.matrix)
            result should be (1)
        }
    }

    "optionToList" should {
        val testGame66 = new Game(Status.Playing , 1, 3)	

        "return List(5, 5) when given Some(\"SuperEasy\")" in {
            testGame66.optionToList(Some("SuperEasy")) shouldEqual List(5, 5)
        }

        "return List(9, 10) when given Some(\"Easy\")" in {
            testGame66.optionToList(Some("Easy")) shouldEqual List(9, 10)
        }

        "return List(15, 40) when given Some(\"Medium\")" in {
            testGame66.optionToList(Some("Medium")) shouldEqual List(15, 40)
        }

        "return List(19, 85) when given Some(\"Hard\")" in {
            testGame66.optionToList(Some("Hard")) shouldEqual List(19, 85)
        }

        "return the default List(9, 10) when given None" in {
            testGame66.optionToList(None) shouldEqual List(9, 10)
        }

        "return the default List(9, 10) when given Some with an unrecognized value" in {
            testGame66.optionToList(Some("Unrecognized")) shouldEqual List(9, 10)
        }
    }

    "def getGame" should {
        val testGame = new Game(Status.Playing, 1, 3)
        testGame.setField()

        "return the game" in {
            val result = testGame
            result should be (testGame)
        }
    }

    "def setState" should {
        val testGame67 = new Game(Status.Playing, 1 , 3)	
        testGame67.setField()

        "return the game" in {
            testGame67.setState(Status.Playing)
            testGame67.state should be (Status.Playing)
        }
    
    }

    "Game object" should {
        val testGame = Game(Status.Playing, 1, 3)
        testGame.setField()

        "return the game" in {
            val result = testGame
            result should be (testGame)
        }
    }




    

}