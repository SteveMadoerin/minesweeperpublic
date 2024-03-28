package de.htwg.se.minesweeper.aview.gui

import de.htwg.se.minesweeper.controller.controllerComponent.IController
import de.htwg.se.minesweeper.util.{Observer, Event, Move}

import de.htwg.se.minesweeper.Default.{given}
import de.htwg.se.minesweeper.Default
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


class GUI(using var controller: IController) extends Frame with Observer:

    controller.add(this)
    private var boardBounds = controller.getFieldSize -1
    var firstMoveControl = new AtomicBoolean(true)
    private var timerStarted: Boolean = false
    var timeLoaded: Boolean = false
    var l = 0
    var m = 0
    var r = 0
    var clock = new AtomicInteger(0)
    val timer = new Timer()
    var task: TimerTask = _

    def startTimer(): Unit = {
        if(task != null){
            task.cancel()
        }
        task = new TimerTask {
            def run: Unit = {
                clock.incrementAndGet()
                setTime
                contents = updateContents
                repaint()
            }
        }
        timerStarted = true
        timer.schedule(task, 1000L, 1000L)
    }

    def stopTimer(): Unit = {
        
        if (task != null) {
            task.cancel()
            task = null // Indicate task is no longer scheduled
            timerStarted = false
        }
    }

    def cells(first: Boolean) = {
        val tmpPanel = new CellPanel(boardBounds+1, boardBounds+1, boardBounds, first)
        tmpPanel.border = borderCreator(0, 2, 2, 2)
        tmpPanel
    }
    
    def statusbar = new BorderPanel{

        val flagCountDisplay = setFlagCountDisplay
        add(new BoxPanel(Orientation.NoOrientation){
            contents ++= flagCountDisplay.productIterator.map(digit => new DigitLabel(digit.asInstanceOf[ImageIcon]))
        }, BorderPanel.Position.West)

        add(new SmileLabel(s"${controller.getSpielbrettState}"), BorderPanel.Position.Center)
        
        val timerDigits = Seq(l, m, r).map(getDigits)
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
                contents += new MenuItem(Action("Help") { controller.helpMenu })
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

    override def update(e: Event): Boolean = {
        e match {

            case Event.NewGame =>
                firstMoveControl.set(true)
                boardBounds = controller.getFieldSize -1
                if(r>0){
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
                    restartTimer(new AtomicInteger(controller.getControllerGame.time))
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
                val text = s"Game is ${controller.getSpielbrettState} and your Score is ${calculateScore}"
                
                resetTimer()
                showMessage(null, text, "GameOver", Message.Info)

                saveScoreNew(saveScore)
                loadScoreNew
                true
            
            case Event.Cheat =>

                val text = s"${controller.getControllerField.gameOverField}"
                showMessage(null, text, "Cheat Menu", Message.Plain)
                false
            
            case Event.Help =>
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
    
                showMessage(null, text, "Help Menu", Message.Info)
                false
            
            case Event.Input =>

                val x = getGraphicalInput
                controller.newGameField(x)
                true
            
            case Event.Load =>

                setLoadedTimer(new AtomicInteger(controller.getControllerGame.time))
                timeLoaded = true
                boardBounds = controller.getFieldSize -1
                contents = updateContents
                repaint()
                true
            
            case Event.Save =>
                
                restartTimer(new AtomicInteger(controller.getControllerGame.time))
                true
            
            case Event.SaveTime =>

                pauseTimer()
                controller.saveTime(clock.get())
                //controller.getControllerGame.updateTime(clock.get())
                contents = updateContents
                repaint()
                true
            
            case Event.Exit => false
        }
    }

    def scaleImage(kind: String): ImageIcon = {
        val specialCases = Map("~" -> "covered", "F" -> "flag", "*" -> "bomb", "" -> "0")
        val numericCases = (0 to 8).map(i => i.toString -> s"$i").toMap
        val imagePaths = (specialCases ++ numericCases).map { case (key, value) => 
            key -> s"src/main/resources/$value.png"
        }
        val imagePath = imagePaths.getOrElse(kind, "src/main/resources/0.png")
        val icon: ImageIcon = new ImageIcon(imagePath)
        val image2: Image = icon.getImage()
        val result = new ImageIcon(getScaledImage(image2, 40, 40))
        result
    }

    def getScaledImage(srcImg: Image, w: Int, h: Int): Image = {
        val resizedImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB)
        val g2: Graphics2D = resizedImg.createGraphics()
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g2.drawImage(srcImg, 0, 0, w, h, null)
        g2.dispose()
        resizedImg
    }

    def getDigits(kind: Int): ImageIcon = {
        val imagePaths = (0 to 9).map(i => i -> s"src/main/resources/time$i.jpeg").toMap
        val imagePath = imagePaths.getOrElse(kind, "src/main/resources/time-.jpeg")
        new ImageIcon(imagePath)
    }

    def getSmiley(kind: String): ImageIcon =
        val imagePath = s"src/main/resources/face$kind.jpeg"
        val icon: ImageIcon = new ImageIcon(imagePath)
        icon
    

    def getGraphicalInput: Option[String] = {
        showInput(null, "Choose Difficulty", "NewGame", Message.Info, Swing.EmptyIcon, List("SuperEasy","Easy", "Medium", "Hard"), "Easy")
    }
    
    def saveScoreNew(saveScore: Int): Unit = {
        val playerName =  showInput(null, "Enter your Name to save your score!", "Save Score", Message.Info, Swing.EmptyIcon, Nil, "Sly").getOrElse("Default")
        val filePath = Default.filePathHighScore
        controller.saveScoreAndPlayerName(playerName, saveScore, Default.filePathHighScore)
    }

    def loadScoreNew: Unit = {
        val filePath = Default.filePathHighScore
        val scores = controller.loadPlayerScores(filePath)
        val top10 = scores.sortBy(-_._2).take(10)

        val message = top10.zipWithIndex.map { case ((name, score), index) =>
            s"${index + 1}. $name: $score"
        }.mkString("\n")

        showMessage(null, message, "Top 10 High Scores", Message.Info)
    }
    
    class CellPanel(x: Int, y: Int, bounds: Int, first: Boolean) extends GridPanel(x,y) {
        (for(
            x <- 0 to bounds;
            y <- 0 to bounds
            ) yield(x,y, controller.showVisibleCell(x,y))).foreach(t => contents += new CellButton(t._1, t._2, t._3, first))
    }
    
    def setTime = synchronized {
        l = clock.get()/100
        m = (clock.get()%100)/10
        r = clock.get()%10
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
        val score = ((controller.getFieldSize * controller.getFieldSize)) * 10 - (l*100+m*10+r)
        if controller.getSpielbrettState == "Won" then score else 0
    
    def resetTimer() = {
        clock.set(0)
        setTime
        timerStarted = false
    }

    def calcFlagCount: Int = controller.getControllerGame.calcMineAndFlag(controller.getControllerField.matrix)

    def setFlagCountDisplay: (ImageIcon, ImageIcon, ImageIcon) =
        val leftDigit =  calcFlagCount / 100
        val middleDigit = calcFlagCount / 10
        val rightDigit = calcFlagCount % 10

        var (resLeft, resMiddle, resRight) = 
            if (calcFlagCount < 0) { 
                val corrRight = (calcFlagCount * -1)%10
                val corrMiddle = (calcFlagCount * -1)/10
                val corrLeft = -1
                (getDigits(corrLeft), getDigits(corrMiddle), getDigits(corrRight))
            } else if (calcFlagCount > 999) {(getDigits(9), getDigits(9), getDigits(9))
            } else {(getDigits(leftDigit), getDigits(middleDigit), getDigits(rightDigit))}
        
        (resLeft, resMiddle, resRight)
    

    class CellButton(x: Int, y: Int, symbols: String, first: Boolean) extends Button(){
        contentAreaFilled = false
        icon = scaleImage(symbols)
        preferredSize_=(new Dimension(40,40))
        maximumSize_=(new Dimension(40,40))
        listenTo(mouse.clicks)
        reactions += {
            case e: MouseClicked if (controller.getControllerGame.board == "Won" || controller.getSpielbrettState == "Lost") =>
            
            case e: MouseClicked if e.peer.getButton == MouseEvent.BUTTON3 => 
                if (controller.showVisibleCell(x,y) == "~"){
                    controller.makeAndPublish(controller.put, Move("flag", y, x))
                } else if (controller.showVisibleCell(x,y) == "F"){
                    controller.makeAndPublish(controller.put, Move("unflag", y, x))
                } else {
                    println(">> You can't flag an open field")
                }
                if first then startTimer()
            
            case e: MouseClicked if e.peer.getButton == MouseEvent.BUTTON1 => 
                synchronized{
                    if (first) {startTimer()}
                    controller.makeAndPublish(controller.doMove, first, Move("open", y, x), controller.getControllerGame)
                    if (first) {
                        controller.checkGameOver(controller.getControllerGame.board)  // check this
                    } else {
                        if(controller.checkGameOver(controller.getControllerGame.board)){controller.gameOver}
                    }
                }
        }
    }

    class SmileLabel(kind: String) extends Label():
        icon = kind match {
            case "Playing" => getSmiley("smile")
            case "Lost" => getSmiley("dead")
            case "Won" => getSmiley("win") 
        }

    class DigitLabel(newIcon: ImageIcon) extends Label():
        icon = newIcon