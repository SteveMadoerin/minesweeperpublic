package de.htwg.sa.minesweeper.ui.gui

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods.{GET, PUT}
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.scaladsl.GraphDSL.Builder
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, Merge, Sink}
import akka.stream.{FlowShape, Materializer, UniformFanInShape, UniformFanOutShape}
import de.htwg.sa.minesweeper.ui.model._

import java.awt.RenderingHints
import java.awt.event.MouseEvent
import java.awt.image.BufferedImage
import java.util.concurrent.atomic._
import java.util.{Timer, TimerTask}
import javax.swing.border.Border
import javax.swing.{BorderFactory, ImageIcon}
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.swing._
import scala.swing.Dialog._
import scala.swing.event.MouseClicked
import scala.util.{Failure, Success}



class GUI() extends Frame:

    //controller.add(this)
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
    val timer = Timer()
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

        add(new SmileLabel(s"${controllerGame.board}"), BorderPanel.Position.Center)
        
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
    def updateContents = {

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
                //val text = s"Game is ${controllerGame.board} and your Score is ${calculateScore}"
                
                resetTimer()
                //showMessage(None.orNull, text, "GameOver", Message.Info)
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
            
            case Event.Help =>

                false
            
            case Event.Input =>

                val x = showGraphicalInput
                RestUtil.requestControllerNewGameFieldGui(x) //controller.newGameField(x)

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
                RestUtil.requestControllerSaveTime(clock.get()) //controller.saveTime(clock.get())
                contents = updateContents
                repaint()
                true
            
            case Event.Exit => System.exit(0); true
        }
    }


    val guiFlow: Flow[HttpRequest, String, NotUsed] = Flow.fromGraph(GraphDSL.create() { implicit builder: Builder[NotUsed] =>
        import GraphDSL.Implicits.*


        val guiGetFlow = Flow[HttpRequest].map { request =>
            request.uri.path.toString match {
                case "/gui/notify" => HttpResponse(entity = "GUI Wrong Request Get")
                case "/gui" => HttpResponse(entity = "GUI")
                case "/gui/hello" => HttpResponse(entity = "hello")
                case _ => HttpResponse(entity = request.uri.path.toString + "not supported get")
            }
        }
        val guiFlowPost = Flow[HttpRequest].mapAsync(1) { request =>
            val result = request.uri.path.toString match {
                case "/gui/notify" =>
                    val event = request.uri.query().get("event").get
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
                        case _ => true

                    Future.successful(HttpResponse(entity = "success notify" + event))

                case _ => Future.successful(HttpResponse(entity = "not supported post" + request.uri.path.toString))
            }
            result

        }


        val getRequestFlowShape = builder.add(guiGetFlow)

        val getResponseFlow = Flow[HttpResponse].mapAsync(1) { response =>
            Unmarshal(response.entity).to[String]
        }

        val getResponseFlowShape = builder.add(getResponseFlow)

        val putRequestFlowShape = builder.add(guiFlowPost)

        val postResponseFlow = Flow[HttpResponse].mapAsync(1) { response =>
            Unmarshal(response.entity).to[String]
        }

        val putResponseFlowShape = builder.add(postResponseFlow)

        val broadcast: UniformFanOutShape[HttpRequest, HttpRequest] = builder.add(Broadcast[HttpRequest](2))
        val merge: UniformFanInShape[String, String] = builder.add(Merge[String](2))

        broadcast.out(0) ~> putRequestFlowShape ~> putResponseFlowShape ~> merge.in(0)
        broadcast.out(1) ~> getRequestFlowShape ~> getResponseFlowShape ~> merge.in(1)


        FlowShape(broadcast.in, merge.out)
    })

    def start(): Unit = {
        val bindFuture = Http().newServerAt("0.0.0.0", 9087).bind(
            pathPrefix("gui") {
                extractRequest { request =>
                    complete(
                        akka.stream.scaladsl.Source.single(request).via(guiFlow).runWith(Sink.head).map(resp => resp)
                    )
                }
            }
        )

        bindFuture.onComplete {
            case Success(binding) =>
                println("Server online at http://localhost:9087/")
                complete(binding.toString)
            case Failure(exception) =>
                println(s"An error occurred: $exception")
                complete("fail binding")
        }
    }

/*    val requestHandler: HttpRequest => HttpResponse = {
        case HttpRequest(GET, Uri.Path("/"), _, _, _) =>
            HttpResponse(entity = HttpEntity(
                ContentTypes.`text/html(UTF-8)`,
                "<html><body>Hello and Welcome to the MineSweeper UI written in Scala by Authors: Steve and Dennis </body></html>"))

        case HttpRequest(GET, Uri.Path("/gui"), _, _, _) =>
            HttpResponse(entity = "gui")

        case HttpRequest(GET, Uri.Path("/gui/hello"), _, _, _) =>
            HttpResponse(entity = "hello")

        case HttpRequest(PUT, Uri.Path("/gui/notify"), _, attrib, _) =>
            // extract the event from the request
            val event = attrib.toString
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

            val text = s"success notify event: $event"
            HttpResponse(entity = text)


        case HttpRequest(GET, Uri.Path("/crash"), _, _, _) =>
            sys.error("BOOM!")

        case r: HttpRequest =>
            r.discardEntityBytes() // important to drain incoming HTTP Entity stream
            HttpResponse(404, entity = "Unknown resource!")
    }*/

/*    val route: Route = {
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
                parameter("event".as[String]) { event =>
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

                    complete("success notify " + event)
                }
            }
        }

    }

    def start(): Unit = {
        val serverSource = Http(system).newServerAt("0.0.0.0", 9087).bind(requestHandler)

        serverSource.onComplete {
            case Success(binding) =>
                println("Server online at http://localhost:9087/")
            case Failure(exception) =>
                println(s"An error occurred: $exception")
                system.terminate()
        }
    }
    */

    val requestHandler: HttpRequest => Future[HttpResponse] = {
        case HttpRequest(GET, Uri.Path("/"), _, _, _) =>
            Future.successful(HttpResponse(entity = HttpEntity(
                ContentTypes.`text/html(UTF-8)`,
                "<html><body>Hello and Welcome to the MineSweeper UI written in Scala by Authors: Steve and Dennis </body></html>")))

        case HttpRequest(GET, Uri.Path("/gui"), _, _, _) =>
            Future.successful(HttpResponse(entity = "gui"))

        case HttpRequest(GET, Uri.Path("/gui/hello"), _, _, _) =>
            Future.successful(HttpResponse(entity = "hello"))

        case HttpRequest(PUT, Uri.Path("/gui/notify"), _, attrib, _) =>
            // extract the event from the request
            val event = attrib.toString
            val updateResult = event match {
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
            }

            val text = s"success notify event: $event"
            Future.successful(HttpResponse(entity = text))

        case HttpRequest(GET, Uri.Path("/crash"), _, _, _) =>
            Future.failed(new Exception("BOOM!"))

        case r: HttpRequest =>
            r.discardEntityBytes() // important to drain incoming HTTP Entity stream
            Future.successful(HttpResponse(404, entity = "Unknown resource!"))
    }


    val requestHandlerFlow: Flow[HttpRequest, HttpResponse, NotUsed] = Flow.fromGraph(GraphDSL.create() { implicit builder =>
        import GraphDSL.Implicits.*

        // Create the various flows for handling specific paths
        val rootFlow = Flow[HttpRequest].collect {
            case HttpRequest(GET, Uri.Path("/"), _, _, _) =>
                HttpResponse(entity = HttpEntity(ContentTypes.`text/html(UTF-8)`, "<html><body>Hello and Welcome to the MineSweeper UI written in Scala by Authors: Steve and Dennis </body></html>"))
        }

        val guiFlow = Flow[HttpRequest].collect {
            case HttpRequest(GET, Uri.Path("/gui"), _, _, _) =>
                HttpResponse(entity = "gui")
        }

        val guiHelloFlow = Flow[HttpRequest].collect {
            case HttpRequest(GET, Uri.Path("/gui/hello"), _, _, _) =>
                HttpResponse(entity = "hello")
        }

        val guiPutUpdateFlow = Flow[HttpRequest].collect {
            case HttpRequest(PUT, Uri.Path("/gui/notify"), _, attrib, _) =>
                // extract the event from the request
                val event = attrib.toString
                val updateResult = event match {
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
                }

                val text = s"success notify event: $event"
                HttpResponse(entity = text)
        }

        // Create a broadcast to feed requests into each flow
        val broadcast = builder.add(Broadcast[HttpRequest](4)) // number of flows
        val merge = builder.add(Merge[HttpResponse](4)) // Must match the number of flows

        // Connect the broadcast to each flow
        broadcast ~> guiPutUpdateFlow ~> merge
        broadcast ~> rootFlow ~> merge
        broadcast ~> guiFlow ~> merge
        broadcast ~> guiHelloFlow ~> merge

        FlowShape(broadcast.in, merge.out) // Expose ports
    })

    def Start(): Unit = {
        val serverSource = Http(system).newServerAt("0.0.0.0", 9087).bindFlow(requestHandlerFlow)

        serverSource.onComplete {
            case scala. util.Success(binding) =>
                println("Server online at http://localhost:9087/")
            case Failure(exception) =>
                println(s"An error occurred: $exception")
                system.terminate()
        }
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
    
    def showGraphicalInput: Option[String] = {
        showInput(None.orNull, "Choose Difficulty", "NewGame", Message.Info, Swing.EmptyIcon, List("SuperEasy","Easy", "Medium", "Hard"), "Easy") // was null before
    }
    
    def saveScoreNew(playerName: String, saveScore: Int): Unit = {

        RestUtil.requestControllerSaveScoreAndPlayerName(saveScore, playerName)
    }

    def loadScoreNew(playername: String, saveScore: Int) = {

        val loadAndDisplayScores = () => {
            //val scores = controller.loadPlayerScores // path is set in Persistence
            val scores = RestUtil.requestLoadPlayerScores
            val newScores = scores :+ (playername, saveScore)
            val top10 = newScores.sortBy(-_._2).take(10)
            //add score
            val message = top10.zipWithIndex.map { case ((name, score), index) =>
                s"${index + 1}. $name: $score"
            }.mkString("\n")
            //add new score list to db
            Dialog.showMessage(None.orNull, message, "Top 10 High Scores", Message.Info)

        }
        loadAndDisplayScores()
    }
        
    class CellPanel(var x: Int, var y: Int, bounds: Int, first: Boolean) extends GridPanel(x,y) {
        //controller.field.matrix.rows.size -1
        //TODO: replace
        if (x == 0 && y == 0) {
            x = 1
            y = 1
        } else if (x == 0 && y > 0) {
            x = 1
        } else if (x > 0 && y == 0) {
            y = 1
        }

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

    /* def calcFlagCount: Int = controller.game.calcMineAndFlag(controller.field.matrix) */
    def calcFlagCount: Int = calcMineAndFlag(controllerField.matrix, controllerGame)

    def prettyFormat(fieldTui: FieldTui): String = {
            fieldTui.matrix.rows.map(_.mkString(" ")).mkString("\n")
    }

    def calcMineAndFlag(visibleMatrix: MatrixTui[String], gamely: GameTui) = {
        (gamely.bombs - calcX("F")(visibleMatrix))
    }

    def calcX(symbols: String)(visibleMatrix: MatrixTui[String]): Int = {
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
            case e: MouseClicked if (updateGame.board == "Won" || updateGame.board == "Lost") =>
                //controllerField = FieldTui(controllerField.hidden, controllerField.hidden)
                update(Event.GameOver)
                val unit = "returns unit"
            
            case e: MouseClicked if e.peer.getButton == MouseEvent.BUTTON3 =>
                // Check if controllerField is up to date
                controllerField = RestUtil.requestControllerField
                if (controllerField.matrix.rows(x)(y) == "~"){
                    //controller.makeAndPublish(controller.put, Move("flag", y, x))
                    RestUtil.requestControllerMakeAndPublishPut(Move("flag", y, x))
                } else if (controllerField.matrix.rows(x)(y) == "F"){
                    //controller.makeAndPublish(controller.put, Move("unflag", y, x))
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
                    //controller.makeAndPublish(controller.doMove, first, Move("open", y, x), controller.game)
                    RestUtil.requestControllerMakeAndPublishDoMove(first, Move("open", y, x), controllerGame)
                    if (first) {
                        //controller.checkGameOver(controllerGame.board)  // check this
                        RestUtil.requestCheckGameOver(controllerGame.board)
                    } else {
                        controllerGame = RestUtil.requestControllerGame
                        if(RestUtil.requestCheckGameOver(controllerGame.board)) {
                            RestUtil.requestGameOver
                        }
                        //if(controller.checkGameOver(controllerGame.board)){controller.gameOver}
                    }
                }
        }
        def updateGame: GameTui = {
            RestUtil.requestControllerGame
        }
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