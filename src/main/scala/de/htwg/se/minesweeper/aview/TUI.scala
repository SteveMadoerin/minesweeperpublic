package de.htwg.se.minesweeper.aview


import de.htwg.se.minesweeper.controller.controllerComponent.IController
import de.htwg.se.minesweeper.util.{Observer, Move, Event}

import scala.io.StdIn.readLine
import scala.util.{Try, Success, Failure}

import scala.compiletime.ops.string
import scala.util.matching.Regex

import de.htwg.se.minesweeper.Default.{given}

class TUI(using var controller: IController) extends Observer:
    
    controller.add(this)

    var game = controller.getControllerGame
    var isFirstMove = true

    def run = 
        infoMessages("Welcome to Minesweeper")
        resize
        parseInputandPrintLoop(isFirstMove)
        
    override def update(e: Event): Boolean = 
      e match
        case Event.NewGame => 
          infoMessages(controller.getControllerField.toString())
          isFirstMove = true
          true
        case Event.Start => infoMessages(controller.getControllerField.toString()); true
        case Event.Next => infoMessages(controller.getControllerField.toString()); true
        case Event.GameOver => infoMessages(controller.getControllerField.toString()); true
        case Event.Cheat => false
        case Event.Help => false
        case Event.Input => false
        case Event.Load => infoMessages(controller.getControllerField.toString()); true
        case Event.Save => false
        case Event.SaveTime => false
        case Event.Exit => System.exit(0); false

    def userInX(rawInput: String): Option[Move] = 
      val cleanInputPattern: Regex = """^[a-z]{1}[0-9]{4}$""".r
      val onlyOneStringPattern: Regex = """^[q|h|r|z|y|o|f|s|l|u|t]{1}$""".r

      val input = rawInput match
        case cleanInputPattern() => rawInput
        case onlyOneStringPattern() => rawInput
        case _ => infoMessages(">> Invalid Input Format");"e"
      
      input match
        case "q" => System.exit(0); None
        case "h" => controller.helpMenu; None
        case "r" => controller.cheat; None
        case "z" => controller.makeAndPublish(controller.undo); None
        case "y" => controller.makeAndPublish(controller.redo); None
        case "s" => controller.saveGame; None
        case "l" => controller.loadGame; None
        case "e" => None // for testing
        case _ => {
          val charAccumulator = input.toCharArray()
          
          val action = charAccumulator(0) match
            case 'o' => "open"
            case 'f' => "flag"
            case 'u' => "unflag"
            case _ => "e"
          
          val coordinates = charArrayToInt(charAccumulator) match{
            case Success(i) => Some(i)
            case Failure(e) => infoMessages(s">> Invalid Move: ${e.getMessage}"); None
          }

          val validCoordinates: Option[Move] = coordinates match {
            case Some(i) => {if game.side > i._1 && game.side > i._2 then Some(Move(action, i._1, i._2)) else { infoMessages(">> Invalid Move: Coordinates out of bounds"); None}}
            case None => None
          }
          validCoordinates
        }

    
    def parseInputandPrintLoop(firstMoveCheck: Boolean): Unit = {
      infoMessages("Enter your move (<action><x><y>, eg. o0102, q to quit, h for help):")
      var stillFirstMove = false
      userInX(readLine) match
        case None => if firstMoveCheck == true then stillFirstMove = true
        case Some(move) => 
          move.value match {
            case "open" => controller.makeAndPublish(controller.doMove, firstMoveCheck, move, game)
            case "flag" => controller.makeAndPublish(controller.put, move)
            case "unflag" => controller.makeAndPublish(controller.put, move)
            case "help" => controller.helpMenu
            case _ => infoMessages(">> Invalid Input")
          }
      
      if !controller.checkGameOver then parseInputandPrintLoop(stillFirstMove) else controller.gameOver; restart
    }

    def restart: Unit = 
      infoMessages("Do you want to play again? (yes/no)")
      readLine match
        case "yes" => run
        case "no" => controller.exit
        case _ => infoMessages(">> Invalid Input");
    
    def charArrayToInt(s: Array[Char]): Try[(Int, Int)] =
      Try(((s(1).toString + s(2).toString).toInt, (s(3).toString + s(4).toString).toInt))
    
    def resize: Unit = 
        val (side, bombs) = chooseDifficulty()
        controller.newGame(side, bombs)
    
    def chooseDifficulty() = {
        val multilineString = 
            """Enter the Difficulty Level
            |Press 0 for SUPEREASY (5 * 5 Cells and 1 Mine)
            |Press 1 for BEGINNER (9 * 9 Cells and 10 Mines)
            |Press 2 for INTERMEDIATE (15 * 15 Cells and 40 Mines)
            |Press 3 for ADVANCED (19 * 19 Cells and 85 Mines)""".stripMargin

        infoMessages(multilineString)

        val ebene = scala.io.StdIn.readLine
        val level = Try(ebene.toInt).getOrElse(1)
        level match {
            case 0 => (5, 5)
            case 1 => (9, 10)
            case 2 => (15, 40)
            case 3 => (19, 85)
            case _ => (9, 10)
        }
    }

    def infoMessages(text: String*) = {
        text.foreach(println)
    }