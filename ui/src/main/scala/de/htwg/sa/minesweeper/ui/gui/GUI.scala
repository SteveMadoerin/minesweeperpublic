package de.htwg.sa.minesweeper.ui.gui

import de.htwg.sa.minesweeper.controller.controllerComponent.IController
import de.htwg.sa.minesweeper.util.{Observer, Event, Move}

import de.htwg.sa.minesweeper.ui.config.Default.{given}
import de.htwg.sa.minesweeper.ui.config.Default
import scala.swing.event.MouseClicked
import scala.swing._
import scala.swing.Button
import scala.swing.Dialog._
import scala.io.Source
import scala.util.Try
import javax.swing.{ImageIcon, Icon}
import javax.swing.border.{Border, MatteBorder, BevelBorder}
import javax.swing.BorderFactory
import java.awt.image.BufferedImage
import java.awt.RenderingHints
import java.awt.event.{ActionListener, ActionEvent, MouseEvent}
import java.util.concurrent.atomic._
import java.util.{Timer, TimerTask}
import java.io.{File, PrintWriter, FileWriter}

import scala.util.{Try, Success, Failure}

import scala.compiletime.ops.string
import scala.util.matching.Regex

import play.api.libs.json.{JsValue, Json}
import scala.io.Source
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.HttpMethods
import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.model.ContentTypes
import akka.http.scaladsl.Http
import scala.concurrent.Future
import akka.http.scaladsl.model.HttpResponse
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContextExecutor
import akka.actor.ActorSystem
import akka.stream.Materializer
import scala.concurrent.Await
import play.api.libs.json.JsObject
import play.api.libs.json.JsString
import scala.annotation.internal.Body
import akka.util.ByteString
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import scala.concurrent.Future
import scala.util.{Failure, Success}

import scala.util.{Try, Success, Failure}
import akka.http.scaladsl.unmarshalling.Unmarshal
import scala.concurrent.Future
import scala.concurrent.duration._
import play.api.libs.json.JsArray
import play.api.libs.json.Format
import play.api.libs.json.JsResult
import play.api.libs.json.JsSuccess
import play.api.libs.json.JsError

import java.nio.file.{Files, Paths}
import scala.util.{Failure, Success}

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, Materializer}
import scala.concurrent.ExecutionContext
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.RouteDirectives
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Route, StandardRoute}
import de.htwg.sa.minesweeper.ui.config.Default.{given}
import scala.concurrent.ExecutionContextExecutor
import scala.util.Failure
import scala.util.Success
import scala.util.matching.Regex
import scala.util.Try
import de.htwg.sa.minesweeper.entity.MatrixDTO
import de.htwg.sa.minesweeper.entity.GameDTO
import de.htwg.sa.minesweeper.entity.FieldDTO



class GUI(using var controller: IController) extends Frame:

    //controller.add(this)
    implicit val system: ActorSystem = ActorSystem()
    implicit val materializer: Materializer = Materializer(system)
    implicit val executionContext: ExecutionContextExecutor = system.dispatcher

    case class GameTui(bombs: Int, side: Int, time: Int, board: String)
    case class FieldTui(matrix: MatrixTui[String], hidden: MatrixTui[String])
    case class MatrixTui[T] (rows: Vector[Vector[T]])

    private var boardBounds = /* controller.field.matrix.rows.size-1 */ RestUtil.requestBoardBounds // TODO: implement
    
    private var firstMoveControl = new AtomicBoolean(true)
    private var timerStarted: Boolean = false
    private var timeLoaded: Boolean = false
    private var digitBar = new DigitDisplay(0, 0, 0)
    var clock = new AtomicInteger(0)
    val timer = new Timer()
    var task: Option[TimerTask] = None

    def startTimer(): Unit = {
        task.foreach(_.cancel())
        task = Some(new TimerTask {
            def run(): Unit = {
                clock.incrementAndGet()
                setTime
                contents = updateContents
                repaint()
            }
        })
        timerStarted = true
        timer.schedule(task.get, 1000L, 1000L)
    }
    
    def stopTimer(): Unit = {
        task.foreach(_.cancel())
        task = None // Indicate task is no longer scheduled
        timerStarted = false
    }
    
    def cells(first: Boolean) = {
        val tmpPanel = new CellPanel(boardBounds+1, boardBounds+1, boardBounds, first)
        tmpPanel.border = borderCreator(0, 2, 2, 2)
        tmpPanel
    }
    
    def statusbar = new BorderPanel{

        val flagCountDisplay = buildFlagCountDisplay
        add(new BoxPanel(Orientation.NoOrientation){
            contents ++= flagCountDisplay.productIterator.map(digit => new DigitLabel(digit.asInstanceOf[ImageIcon]))
        }, BorderPanel.Position.West)

        add(new SmileLabel(s"${controller.game.board}"), BorderPanel.Position.Center)
        
        val timerDigits = Seq(digitBar.leftDigit, digitBar.middleDigit, digitBar.rightDigit).map(showDigits)
        add(new BoxPanel(Orientation.NoOrientation){
            contents ++= timerDigits.map(new DigitLabel(_))
        }, BorderPanel.Position.East)

        border = borderCreator(2, 2, 2, 2)
    }

    def borderCreator(top: Int, left: Int, bottom: Int, right: Int): Border = {
        val borderOutside: Border = BorderFactory.createLineBorder(java.awt.Color.GRAY)
        val borderInside: Border = BorderFactory.createMatteBorder(top, left, bottom, right, java.awt.Color.WHITE)
        val mergedBorder: Border = BorderFactory.createCompoundBorder(borderOutside, borderInside)
        mergedBorder
    }

    def run = {

        title = "Minesweeper"
        menuBar = new MenuBar{
            contents += new Menu("Game"){
                contents += new MenuItem(Action("Quit") { controller.exit })
                contents += new MenuItem(Action("New") { controller.newGameGUI})
                contents += new MenuItem(Action("Load") { controller.loadGame })
                contents += new MenuItem(Action("Save") { controller.saveGame })
            }
            contents += new Menu("Edit"){
                contents += new MenuItem(Action("Undo") { controller.makeAndPublish(controller.undo) })
                contents += new MenuItem(Action("Redo") { controller.makeAndPublish(controller.redo) })
            }
            contents += new Menu("Help"){
                contents += new MenuItem(Action("Help") { showHelp/* controller.helpMenu */ }) // TODO:
                contents += new MenuItem(Action("Cheat") { controller.cheat })
            }
        }
        
        resizable = false
        contents = updateContents
        pack()
        centerOnScreen()
        open()
    }

    def updateContents = {
        
        val firstCheck = firstMoveControl.get()
        val temp = new BorderPanel{
            add(statusbar, BorderPanel.Position.North)
            add(cells(firstCheck), BorderPanel.Position.Center)
        }
        firstMoveControl.set(false)
        temp

    }

    def update(e: Event): Boolean = {
        e match {

            case Event.NewGame =>
                firstMoveControl.set(true)
                boardBounds = controller.field.matrix.rows.size -1
                if(digitBar.rightDigit>0){
                    stopTimer()
                    resetTimer()
                }

                contents = updateContents
                repaint()
                true
            
            case Event.Start =>

                if(!timerStarted){
                    startTimer()
                } else{
                    contents = updateContents
                    repaint()
                }
                true
            
            case Event.Next =>
                
                if(!timerStarted){
                    restartTimer(new AtomicInteger(controller.game.time))
                } else{
                    contents = updateContents
                    repaint()
                }
                true
            
            case Event.GameOver =>

                contents = updateContents
                repaint()
                stopTimer()

                val saveScore: Int = calculateScore
                val text = s"Game is ${controller.game.board} and your Score is ${calculateScore}"
                
                resetTimer()
                showMessage(None.orNull, text, "GameOver", Message.Info)

                saveScoreNew(saveScore)
                loadScoreNew
                true
            
            case Event.Cheat =>

                /* val text = s"${controller.field.gameOverField}" */
                val text = s"${prettyFormat(FieldDTO(controller.field.hidden, controller.field.hidden))}"
                showMessage(None.orNull, text, "Cheat Menu", Message.Plain)
                false
            
            case Event.Help =>

                false
            
            case Event.Input =>

                val x = showGraphicalInput
                controller.newGameField(x)
                true
            
            case Event.Load =>

                setLoadedTimer(new AtomicInteger(controller.game.time))
                timeLoaded = true
                boardBounds = controller.field.matrix.rows.size -1
                contents = updateContents
                repaint()
                true
            
            case Event.Save =>
                
                restartTimer(new AtomicInteger(controller.game.time))
                true
            
            case Event.SaveTime =>

                pauseTimer()
                controller.saveTime(clock.get())
                contents = updateContents
                repaint()
                true
            
            case Event.Exit => false
        }
    }

    val route: Route = {
        get {
            path("gui") {
                complete("gui")
            } ~ 
            path("gui"/"hello") {
                complete("hello")
            } 
        } ~
        put {
            path("gui"/"notify") {
                    parameter("event".as[String]) { (event) =>
                        //
                        event match
                            case "NewGame" => update(Event.NewGame)
                            case "Start" => update(Event.Start)
                            case "Next" => update(Event.Next)
                            case "GameOver" => update(Event.GameOver)
                            case "Cheat" => update(Event.Cheat)
                            case "Help" => update(Event.Help)
                            case "Input" => update(Event.Input)
                            case "Load" => update(Event.Load)
                            case "Save" => update(Event.Save)
                            case "SaveTime" => update(Event.SaveTime)
                            case "Exit" => update(Event.Exit)
                            case _ => false
                            
                        complete("success notify" + event)
                    }
            }
        }
        
    }


    val bindFuture = Http().bindAndHandle(route, "localhost", 8087)

    def unbind = bindFuture
        .flatMap(_.unbind())
        .onComplete(_ => system.terminate())

    def showHelp: Unit = {
        val text = 
            """This is Minesweeper Help - Menu
            |                                                                                                    
            |Left klick on a cell to open. Avoid any Bombs. If you see a number         
            |eg. 1 it means it has a bomb in one of the surronding neighboring cells.   
            |You can flag a cell, where u think there is a bomb.                        
            |                                                        
            |Now lets play and have fun :-)                                             
            |                                                        
            |Copyright: Steve Madoerin                                                  
            |                                                   
            |""".stripMargin

        showMessage(None.orNull, text, "Help Menu", Message.Info)
    }
    
    def scaleImage(kind: String): ImageIcon = {
        val specialCases = Map("~" -> "covered", "F" -> "flag", "*" -> "bomb", "" -> "0",  " " -> "0")
        val numericCases = (0 to 8).map(i => i.toString -> s"$i").toMap
        val imagePaths = (specialCases ++ numericCases).map { case (key, value) => 
            key -> s"src/main/resources/$value.png"
        }
        val imagePath = imagePaths.getOrElse(kind, "src/main/resources/0.png")
        val icon: ImageIcon = new ImageIcon(imagePath)
        val image2: Image = icon.getImage()
        val result = new ImageIcon(showScaledImage(image2, 40, 40))
        result
    }

    def showScaledImage(srcImg: Image, w: Int, h: Int): Image = {
        val resizedImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB)
        val g2: Graphics2D = resizedImg.createGraphics()
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g2.drawImage(srcImg, 0, 0, w, h, None.orNull)
        g2.dispose()
        resizedImg
    }

    def showDigits(kind: Int): ImageIcon = {
        val imagePaths = (0 to 9).map(i => i -> s"src/main/resources/time$i.jpeg").toMap
        val imagePath = imagePaths.getOrElse(kind, "src/main/resources/time-.jpeg")
        new ImageIcon(imagePath)
    }

    def showSmiley(kind: String): ImageIcon =
        val imagePath = s"src/main/resources/face$kind.jpeg"
        val icon: ImageIcon = new ImageIcon(imagePath)
        icon
    
    def showGraphicalInput: Option[String] = {
        showInput(None.orNull, "Choose Difficulty", "NewGame", Message.Info, Swing.EmptyIcon, List("SuperEasy","Easy", "Medium", "Hard"), "Easy") // was null before
    }
    
    def saveScoreNew(saveScore: Int): Unit = {
        val playerName =  showInput(None.orNull, "Enter your Name to save your score!", "Save Score", Message.Info, Swing.EmptyIcon, Nil, "Sly").getOrElse("Default") // was null before
        val filePath = Default.filePathHighScore
        controller.saveScoreAndPlayerName(playerName, saveScore, Default.filePathHighScore)
    }

    def loadScoreNew = {

        val loadAndDisplayScores = () => {
            val scores = controller.loadPlayerScores // path is set in Persistence
            val top10 = scores.sortBy(-_._2).take(10)
            val message = top10.zipWithIndex.map { case ((name, score), index) =>
                s"${index + 1}. $name: $score"
            }.mkString("\n")

            Dialog.showMessage(None.orNull, message, "Top 10 High Scores", Message.Info)
        }
        loadAndDisplayScores()
    }
        
    class CellPanel(x: Int, y: Int, bounds: Int, first: Boolean) extends GridPanel(x,y) {
        (for(
            x <- 0 to bounds;
            y <- 0 to bounds
            ) yield(x,y, controller.field.matrix.rows(x)(y))).foreach(t => contents += new CellButton(t._1, t._2, t._3, first))
    }
    
    def setTime = synchronized {
        digitBar = DigitDisplay(clock.get()/100, (clock.get()%100)/10, clock.get()%10)
    }

    def restartTimer(loadedTime: AtomicInteger) ={
        clock.set(loadedTime.get())
        setTime
        startTimer()
    }

    def setLoadedTimer(loadedTime: AtomicInteger) = {
        if(timerStarted){
            stopTimer()
        }
        clock.set(loadedTime.get())
        setTime
    }

    def pauseTimer(): Unit = {
        stopTimer()
    }

    def calculateScore: Int =
        val score = ((controller.field.matrix.rows.size * controller.field.matrix.rows.size)) * 10 - (digitBar.leftDigit*100+digitBar.middleDigit*10+digitBar.rightDigit)
        if controller.game.board == "Won" then score else 0
    
    def resetTimer() = {
        clock.set(0)
        setTime
        timerStarted = false
    }

    /* def calcFlagCount: Int = controller.game.calcMineAndFlag(controller.field.matrix) */
    def calcFlagCount: Int = calcMineAndFlag(controller.field.matrix, controller.game)

    def prettyFormat(fieldDto: FieldDTO): String = {
        fieldDto.matrix.rows.map(_.mkString(" ")).mkString("\n")
    }

    def calcMineAndFlag(visibleMatrix: MatrixDTO[String], gamely: GameDTO) = {
        (gamely.bombs - calcX("F")(visibleMatrix))
    }

    def calcX(symbols: String)(visibleMatrix: MatrixDTO[String]): Int = {
        val sizze = visibleMatrix.rows.size -1
        val multiIndex = 0 to sizze
        multiIndex
          .flatMap(row => multiIndex.map(col => (row, col)))
          .count{ case(row, col) => visibleMatrix.rows(row)(col) == symbols && isValid(row, col, sizze)}
    
    }
    def isValid(row: Int, col: Int, side: Int): Boolean = {row >= 0 && row <= side && col >= 0 && col <= side}

    def buildFlagCountDisplay: (ImageIcon, ImageIcon, ImageIcon) =
        val leftDigit =  calcFlagCount / 100
        val middleDigit = calcFlagCount / 10
        val rightDigit = calcFlagCount % 10

        var (resLeft, resMiddle, resRight) = 
            if (calcFlagCount < 0) { 
                val corrRight = (calcFlagCount * -1)%10
                val corrMiddle = (calcFlagCount * -1)/10
                val corrLeft = -1
                (showDigits(corrLeft), showDigits(corrMiddle), showDigits(corrRight))
            } else if (calcFlagCount > 999) {(showDigits(9), showDigits(9), showDigits(9))
            } else {(showDigits(leftDigit), showDigits(middleDigit), showDigits(rightDigit))}
        
        (resLeft, resMiddle, resRight)
    

    class CellButton(x: Int, y: Int, symbols: String, first: Boolean) extends Button(){
        contentAreaFilled = false
        icon = scaleImage(symbols)
        preferredSize_=(new Dimension(40,40))
        maximumSize_=(new Dimension(40,40))
        listenTo(mouse.clicks)
        reactions += {
            case e: MouseClicked if (controller.game.board == "Won" || controller.game.board == "Lost") =>
            
            case e: MouseClicked if e.peer.getButton == MouseEvent.BUTTON3 => 
                if (/* controller.field.showVisibleCell(x,y) */controller.field.matrix.rows(x)(y) == "~"){
                    controller.makeAndPublish(controller.put, Move("flag", y, x))
                } else if (controller.field.matrix.rows(x)(y) == "F"){
                    controller.makeAndPublish(controller.put, Move("unflag", y, x))
                } else {
                    println(">> You can't flag an open field")
                }
                if first then startTimer()
            
            case e: MouseClicked if e.peer.getButton == MouseEvent.BUTTON1 => 
                synchronized{
                    if (first) {startTimer()}
                    controller.makeAndPublish(controller.doMove, first, Move("open", y, x), controller.game)
                    if (first) {
                        controller.checkGameOver(controller.game.board)  // check this
                    } else {
                        if(controller.checkGameOver(controller.game.board)){controller.gameOver}
                    }
                }
        }
    }
    
    class SmileLabel(kind: String) extends Label():
        icon = kind match {
            case "Playing" => showSmiley("smile")
            case "Lost" => showSmiley("dead")
            case "Won" => showSmiley("win") 
        }

    class DigitLabel(newIcon: ImageIcon) extends Label():
        icon = newIcon
    
    case class DigitDisplay(leftDigit: Int, middleDigit: Int, rightDigit: Int)

end GUI