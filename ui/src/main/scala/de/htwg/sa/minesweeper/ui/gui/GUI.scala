package de.htwg.sa.minesweeper.ui.gui

import akka.actor.ActorSystem
import akka.stream.Materializer
import de.htwg.sa.minesweeper.ui.model._

import java.awt.RenderingHints
import java.awt.event.MouseEvent
import java.awt.image.BufferedImage
import java.util.concurrent.atomic._
import java.util.{Timer, TimerTask}
import javax.swing.border.Border
import javax.swing.{BorderFactory, ImageIcon}
import scala.concurrent.ExecutionContextExecutor
import scala.swing._
import scala.swing.Dialog._
import scala.swing.event.MouseClicked



class GUI() extends Frame:
    
    implicit val system: ActorSystem = ActorSystem("gui")
    implicit val materializer: Materializer = Materializer(system)
    implicit val executionContext: ExecutionContextExecutor = system.dispatcher

    private var boardBounds =  RestUtil.requestBoardBounds
    var controllerField: FieldTui = RestUtil.requestControllerField
    var controllerGame: GameTui = RestUtil.requestControllerGame

    private var firstMoveControl = new AtomicBoolean(true)
    private var timerStarted: Boolean = false
    private var timeLoaded: Boolean = false
    private var digitBar = DigitDisplay(0, 0, 0)
    var clock = new AtomicInteger(0)
    val timer: Timer = Timer()
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
        task = None
        timerStarted = false
    }

    def cells(first: Boolean): CellPanel = {
        val tmpPanel = new CellPanel(boardBounds+1, boardBounds+1, boardBounds, first)
        tmpPanel.border = borderCreator(0, 2, 2, 2)
        tmpPanel
    }

    def statusbar: BorderPanel = new BorderPanel{

        val flagCountDisplay: (ImageIcon, ImageIcon, ImageIcon) = buildFlagCountDisplay
        add(new BoxPanel(Orientation.NoOrientation){
            contents ++= flagCountDisplay.productIterator.map(digit => new DigitLabel(digit.asInstanceOf[ImageIcon]))
        }, BorderPanel.Position.West)

        add(new SmileLabel(s"${controllerGame.board}"), BorderPanel.Position.Center)

        val timerDigits: Seq[ImageIcon] = Seq(digitBar.leftDigit, digitBar.middleDigit, digitBar.rightDigit).map(showDigits)
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
                contents += new MenuItem(Action("Quit") { System.exit(0) })
                contents += new MenuItem(Action("Easy") {
                    val (newField, newGame) = RestUtil.requestNewGame(5,5)
                    controllerField = newField
                    controllerGame = newGame
                })
                contents += new MenuItem(Action("Medium") {
                    val (newField, newGame) = RestUtil.requestNewGame(9,10)
                    controllerField = newField
                    controllerGame = newGame})
                contents += new MenuItem(Action("Hard") {
                    val (newField, newGame) = RestUtil.requestNewGame(12,15)
                    controllerField = newField
                    controllerGame = newGame
                })
                contents += new MenuItem(Action("Load") { RestUtil.requestControllerLoadGame})
                contents += new MenuItem(Action("Save") { RestUtil.requestControllerSaveGame})
            }
            contents += new Menu("Edit"){
                contents += new MenuItem(Action("Undo") { RestUtil.requestControllerMakeAndPublishUndo })
                contents += new MenuItem(Action("Redo") { RestUtil.requestControllerMakeAndPublishRedo })
            }
            contents += new Menu("Help"){
                contents += new MenuItem(Action("Help") { showHelp})
                contents += new MenuItem(Action("Cheat") { update(Event.Cheat) })
            }
        }

        resizable = false
        contents = updateContents
        pack()
        centerOnScreen()
        open()
    }
    var b = true
    def updateContents: BorderPanel = {

        if ((controllerGame.board == "Lost" || controllerGame.board == "Won") && b) {
            b = false
            update(Event.GameOver)
        }
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
                b = true
                controllerGame = RestUtil.requestControllerGame
                controllerField = RestUtil.requestControllerField

                firstMoveControl.set(true)
                boardBounds = controllerField.matrix.rows.size -1
                if(digitBar.rightDigit>0 || clock.get()>0){
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
                    restartTimer(new AtomicInteger(controllerGame.time))
                } else{
                    contents = updateContents
                    repaint()
                }
                true

            case Event.GameOver =>
                val gameOverField = FieldTui(controllerField.hidden, controllerField.hidden)
                controllerField = gameOverField

                contents = updateContents
                repaint()
                stopTimer()

                val saveScore: Int = calculateScore
                controllerGame = RestUtil.requestControllerGame
                resetTimer()
                val playerName = showInput(None.orNull, "Enter your Name to save your score!", "Save Score", Message.Info, Swing.EmptyIcon, Nil, "Sly").getOrElse("Default") // was null before
                print(s"playerName: $playerName, saveScore: $saveScore")
                loadScoreNew(playerName, saveScore)
                saveScoreNew(playerName, saveScore)
                true

            case Event.Cheat =>

                val cheatingField = RestUtil.requestControllerField
                val text = s"${prettyFormat(FieldTui(cheatingField.hidden, cheatingField.hidden))}"
                showMessage(None.orNull, text, "Cheat Menu", Message.Plain)
                false

            case Event.Help => false
            case Event.Input =>

                val x = showGraphicalInput
                RestUtil.requestControllerNewGameFieldGui(x)
                true

            case Event.Load =>
                controllerGame = RestUtil.requestControllerGame
                setLoadedTimer(new AtomicInteger(controllerGame.time))
                timeLoaded = true
                boardBounds = controllerField.matrix.rows.size -1
                contents = updateContents
                repaint()
                true

            case Event.Save =>

                restartTimer(new AtomicInteger(clock.get()))
                true

            case Event.SaveTime =>

                pauseTimer()
                RestUtil.requestControllerSaveTime(clock.get())
                contents = updateContents
                repaint()
                true

            case Event.Exit => System.exit(0); true
        }
    }

    def start: Unit = {
        val updateFunction: Event => Unit = (event: Event) => {
            update(event)
            ()
        }

        val kafkaProducer = new GuiNotificationConsumer(system)(updateFunction)
        kafkaProducer.startConsuming()
    }
    
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

    def showGraphicalInput: Option[String] = { showInput(None.orNull, "Choose Difficulty", "NewGame", Message.Info, Swing.EmptyIcon, List("SuperEasy","Easy", "Medium", "Hard"), "Easy") }
    def saveScoreNew(playerName: String, saveScore: Int): Unit = { RestUtil.requestControllerSaveScoreAndPlayerName(saveScore, playerName) }

    def loadScoreNew(playername: String, saveScore: Int) = {

        val loadAndDisplayScores = () => {
            val scores = RestUtil.requestLoadPlayerScores
            val newScores = scores :+ (playername, saveScore)
            val top10 = newScores.sortBy(-_._2).take(10)
            val message = top10.zipWithIndex.map { case ((name, score), index) =>
                s"${index + 1}. $name: $score"
            }.mkString("\n")
            Dialog.showMessage(None.orNull, message, "Top 10 High Scores", Message.Info)
        }
        loadAndDisplayScores()
    }

    class CellPanel(var x: Int, var y: Int, bounds: Int, first: Boolean) extends GridPanel(x,y) {
        controllerField = RestUtil.requestControllerField
        (for(
            x <- 0 to bounds;
            y <- 0 to bounds
        ) yield(x,y, controllerField.matrix.rows(x)(y))).foreach(t => contents += new CellButton(t._1, t._2, t._3, first))
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
        val score = ((controllerField.matrix.rows.size * controllerField.matrix.rows.size)) * 10 - (digitBar.leftDigit*100+digitBar.middleDigit*10+digitBar.rightDigit)
        if controllerGame.board == "Won" then score else 0

    def resetTimer() = {
        clock.set(0)
        setTime
        timerStarted = false
    }
    
    def calcFlagCount: Int = calcMineAndFlag(controllerField.matrix, controllerGame)
    def prettyFormat(fieldTui: FieldTui): String = { fieldTui.matrix.rows.map(_.mkString(" ")).mkString("\n") }
    def calcMineAndFlag(visibleMatrix: MatrixTui[String], gamely: GameTui): Int = { (gamely.bombs - calcX("F")(visibleMatrix)) }
    def isValid(row: Int, col: Int, side: Int): Boolean = {row >= 0 && row <= side && col >= 0 && col <= side}
    
    def calcX(symbols: String)(visibleMatrix: MatrixTui[String]): Int = {
        val sizze = visibleMatrix.rows.size -1
        val multiIndex = 0 to sizze
        multiIndex
          .flatMap(row => multiIndex.map(col => (row, col)))
          .count{ case(row, col) => visibleMatrix.rows(row)(col) == symbols && isValid(row, col, sizze)}

    }
    
    def buildFlagCountDisplay: (ImageIcon, ImageIcon, ImageIcon) =
        val leftDigit =  calcFlagCount / 100
        val middleDigit = calcFlagCount / 10
        val rightDigit = calcFlagCount % 10

        val (resLeft, resMiddle, resRight) =
            if (calcFlagCount < 0) {
                val corrRight = (calcFlagCount * -1) % 10
                val corrMiddle = (calcFlagCount * -1) / 10
                val corrLeft = -1
                (showDigits(corrLeft), showDigits(corrMiddle), showDigits(corrRight))
            } else if (calcFlagCount > 999) {
                (showDigits(9), showDigits(9), showDigits(9))
            } else {
                (showDigits(leftDigit), showDigits(middleDigit), showDigits(rightDigit))
            }

        (resLeft, resMiddle, resRight)

    class CellButton(x: Int, y: Int, symbols: String, first: Boolean) extends Button(){
        contentAreaFilled = false
        icon = scaleImage(symbols)
        preferredSize_=(new Dimension(40,40))
        maximumSize_=(new Dimension(40,40))
        listenTo(mouse.clicks)
        reactions += {
            case e: MouseClicked if (updateGame.board == "Won" || updateGame.board == "Lost") =>
                update(Event.GameOver)
                val unit = "returns unit"

            case e: MouseClicked if e.peer.getButton == MouseEvent.BUTTON3 =>
                controllerField = RestUtil.requestControllerField
                if (controllerField.matrix.rows(x)(y) == "~"){
                    RestUtil.requestControllerMakeAndPublishPut(Move("flag", y, x))
                } else if (controllerField.matrix.rows(x)(y) == "F"){
                    RestUtil.requestControllerMakeAndPublishPut(Move("unflag", y, x))
                } else {
                    println(">> You can't flag an open field")
                }
                if first then startTimer()

            case e: MouseClicked if e.peer.getButton == MouseEvent.BUTTON1 =>
                synchronized{
                    controllerField = RestUtil.requestControllerField
                    controllerGame = RestUtil.requestControllerGame
                    if (first) {startTimer()}
                    RestUtil.requestControllerMakeAndPublishDoMove(first, Move("open", y, x), controllerGame)
                    if (first) {
                        RestUtil.requestCheckGameOver(controllerGame.board)
                    } else {
                        controllerGame = RestUtil.requestControllerGame
                        if(RestUtil.requestCheckGameOver(controllerGame.board)) {
                            RestUtil.requestGameOver
                        }
                    }
                }
        }
        def updateGame: GameTui = { RestUtil.requestControllerGame }
    }

    class SmileLabel(kind: String) extends Label(){
        icon = kind match {
            case "Playing" => showSmiley("smile")
            case "Lost" => showSmiley("dead")
            case "Won" => showSmiley("win")
        }
    }

    class DigitLabel(newIcon: ImageIcon) extends Label():
        icon = newIcon


end GUI