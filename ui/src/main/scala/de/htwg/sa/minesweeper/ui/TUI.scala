package de.htwg.sa.minesweeper.ui


import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import de.htwg.sa.minesweeper.ui.model._
import play.api.libs.json.{JsValue, Json}

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContextExecutor, Future}
import scala.io.StdIn.readLine
import scala.languageFeature.reflectiveCalls
import scala.util.matching.Regex
import scala.util.{Failure, Success, Try}

class TUI():

    //controller.add(this)
    implicit val system: ActorSystem = ActorSystem()
    implicit val materializer: Materializer = Materializer(system)
    implicit val executionContext: ExecutionContextExecutor = system.dispatcher

    var firstMoveCheck = true

    var controllerGame: GameTui = requestControllerGame
    var controllerField: FieldTui = requestControllerField

    def run =
        infoMessages("Welcome to Minesweeper")
        resize
        parseInputandPrintLoop(firstMoveCheck)

    def update(e: Event): Boolean =
        e match
            case Event.NewGame  => infoMessages(requestControllerFieldToString); firstMoveCheck = true; true
            case Event.Load => infoMessages(requestControllerFieldToString); firstMoveCheck = false; true
            case Event.Start | Event.Next  => infoMessages(requestControllerFieldToString); true
            case Event.GameOver => infoMessages(s"The Game is ${requestControllerGame.board} !", requestControllerFieldToString); true
            case Event.Exit => System.exit(0); false
            case Event.Help => infoMessages(requestControllerFieldToString); true
            case _ => false

    def userInX(rawInput: String): Option[Move] = {
        val cleanInputPattern: Regex = """^[a-z]{1}[0-9]{4}$""".r
        val onlyOneStringPattern: Regex = """^[q|h|r|z|y|o|f|s|l|u|t]{1}$""".r

        val input = rawInput match
            case cleanInputPattern() => rawInput
            case onlyOneStringPattern() => rawInput
            case _ => infoMessages(">> Invalid Input Format");"e"

        input match
            case "q" => System.exit(0); None
            case "h" => requestControllerHelpMenue; None
            case "r" => requestControllerCheat; None
            case "z" => requestControllerMakeAndPublishUndo; None
            case "y" => requestControllerMakeAndPublishRedo; None
            case "s" => requestControllerSaveGame; None
            case "l" => requestControllerLoadGame; None
            case "e" => None
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
                controllerGame = requestControllerGame

                val validCoordinates: Option[Move] = coordinates match {
                    case Some(i) => {if controllerGame.side > i._1 && controllerGame.side > i._2 then Some(Move(action, i._1, i._2)) else { infoMessages(">> Invalid Move: Coordinates out of bounds"); None}}
                    case None => None
                }
                validCoordinates
            }

    }

    def parseInputandPrintLoop(firstMoveCheck: Boolean): Unit = {
        infoMessages("Enter your move (<action><x><y>, eg. o0102, q to quit, h for help):")
        val stillFirstMove = userInX(readLine) match {
            case None => firstMoveCheck
            case Some(move) =>
                processMove(move, firstMoveCheck)
                false
        }

        controllerGame = requestControllerGame // replace controller.checkGameOver

        requestCheckGameOver(controllerGame.board) match {
            case false =>
                parseInputandPrintLoop(stillFirstMove)
            case true =>
                requestGameOver
                restart
        }
    }


    def processMove(move: Move, firstMoveCheck: Boolean): Boolean = {
        controllerGame = requestControllerGame
        move.value match {
            case "open" => requestControllerMakeAndPublishDoMove(firstMoveCheck, move, controllerGame); true
            case "flag" => requestControllerMakeAndPublishPut(move); true
            case "unflag" => requestControllerMakeAndPublishPut(move); true
            case _ => infoMessages(">> Invalid Input"); false
        }
    }

    def restart =
        infoMessages("Do you want to play again? (yes/no)")
        readLine match
            case "yes" => run
            case "no" => requestExit
            case _ => infoMessages(">> Invalid Input");

    def charArrayToInt(s: Array[Char]): Try[(Int, Int)] = Try(((s(1).toString + s(2).toString).toInt, (s(3).toString + s(4).toString).toInt))

    def resize: Unit =
        val (side, bombs) = chooseDifficulty()

        requestNewGame(side, bombs)
    // maybe later - receive Game and Field from controller

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

    def requestNewGame(side: Int, bombs: Int) = {

        val url = "http://controller:9081/controller/newGame" + s"?side=$side&bombs=$bombs"
        val request = HttpRequest(
            method =  HttpMethods.PUT,
            uri = url
        )

        val responseFuture: Future[HttpResponse] = Http().singleRequest(request)
        val bodyStringFuture: Future[String] = responseFuture.flatMap { response =>
            response.entity.toStrict(5.seconds).map(_.data.utf8String)
        }
        val bodyString = Await.result(bodyStringFuture, 5.seconds)
        val (newGame, newField): (GameTui, FieldTui) = jsonToGameAndField(bodyString)
        //println(newGame.bombs)
        //println(newField.hidden.rows.size)
        //println("controller.newGame(side, bombs)")

        controllerField = newField
        controllerGame = newGame
    }

    def requestExit = {
        val url = "http://controller:9081/controller/exit"

        val request = HttpRequest(
            method =  HttpMethods.PUT,
            uri = url
        )
    }

    def requestCheckGameOver(status: String) = {
        val url = "http://controller:9081/controller/checkGameOver"

        val request = HttpRequest(
            method =  HttpMethods.PUT,
            uri = url
        ).withEntity(status)

        val responseFuture: Future[HttpResponse] = Http().singleRequest(request)
        val bodyStringFuture: Future[String] = responseFuture.flatMap { response =>
            response.entity.toStrict(5.seconds).map(_.data.utf8String)
        }
        val bodyString = Await.result(bodyStringFuture, 5.seconds)
        bodyString.toBoolean
    }

    def infoMessages(text: String*) = { text.foreach(println) }

    def requestGameOver = {

        val url = "http://controller:9081/controller/gameOver"

        val request = HttpRequest(
            method =  HttpMethods.GET,
            uri = url
        )

        //val bodyString = Await.result(Http().singleRequest(request).flatMap(_.entity.toStrict(5.seconds).map(_.data.utf8String)), 5.seconds)
        //val field = jsonToFieldTui(bodyString)
        //implement maybe later controllerfield = field ...
        //controller.gameOver
    }

    def requestControllerField = {
        //controller.field
        val url = "http://controller:9081/controller/field"

        val request = HttpRequest(
            method =  HttpMethods.GET,
            uri = url
        )

        val bodyString = Await.result(Http().singleRequest(request).flatMap(_.entity.toStrict(5.seconds).map(_.data.utf8String)), 5.seconds)
        val field = jsonToFieldTui(bodyString)
        field
    }

    def jsonToFieldTui(jsonString: String): FieldTui = {
        val json: JsValue = Json.parse(jsonString)
        val size = (json \ "field" \ "size").get.toString.toInt

        val fieldVectorOption: Option[FieldTui] = Some(FieldTui(MatrixTui(Vector.tabulate(size, size) {(row, col) => "E"}), (MatrixTui(Vector.tabulate(size, size) {(row, col) => "E"}))))
        val matrixVectorOption: Option[Vector[Vector[String]]] = Some(Vector.tabulate(size, size) {(row, col) => "E"})
        val hiddenVectorOption: Option[Vector[Vector[String]]] = Some(Vector.tabulate(size, size) {(row, col) => "E"})

        val matrixVector1 = matrixVectorOption match{
            case Some(matrix) => matrix
            case None => println("Matrix is not valid"); Vector.tabulate(size, size) {(row, col) => "E"}
        }

        val updatedMatrixVector: Vector[Vector[String]] = (0 until size * size).foldLeft(matrixVector1) {
            case (currentMatrix, index) =>
                val row = (json \ "field" \ "matrix" \\ "row")(index).as[Int]
                val col = (json \ "field" \ "matrix" \\ "col")(index).as[Int]
                val cell = (json \ "field" \ "matrix" \\ "cell")(index).as[String]
                currentMatrix.updated(row, currentMatrix(row).updated(col, cell))
        }

        val hiddenVector1 = hiddenVectorOption match{
            case Some(m) => m
            case None => println("Hidden is not valid"); Vector.tabulate(size, size) {(row, col) => "E"}
        }

        val updatedHiddenVector: Vector[Vector[String]] = (0 until size * size).foldLeft(hiddenVector1) {
            case (currentHidden, index) =>
                val row = (json \ "field" \ "hidden" \\ "row")(index).as[Int]
                val col = (json \ "field" \ "hidden" \\ "col")(index).as[Int]
                val cell = (json \ "field" \ "hidden" \\ "cell")(index).as[String]
                currentHidden.updated(row, currentHidden(row).updated(col, cell))
        }

        val finalFieldOption = fieldVectorOption match{
            case Some(f) => Some(FieldTui(MatrixTui(updatedMatrixVector), MatrixTui(updatedHiddenVector)))
            case None => println("Field is not valid"); None
        }

        finalFieldOption.get
    }


    def requestControllerGame: GameTui = {

        val url = "http://controller:9081/controller/game"

        val request = HttpRequest(
            method =  HttpMethods.GET,
            uri = url
        )
        val bodyString = Await.result(Http().singleRequest(request).flatMap(_.entity.toStrict(5.seconds).map(_.data.utf8String)), 5.seconds)
        val game = jsonToGameTui(bodyString)

        game //controller.game.asInstanceOf[Game]
    }

    def jsonToGameTui(jsonString: String): GameTui = {
        val json: JsValue = Json.parse(jsonString)
        val status = (json \ "game" \ "status").get.toString
        val bombs = (json \ "game" \ "bombs").get.toString.toInt
        val side = (json \ "game" \ "side").get.toString.toInt
        val time = (json \ "game" \ "time").get.toString.toInt
        val statusWithoutQuotes = status.replace("\"", "") // \Playing\ -> Playing
        GameTui(bombs, side, time, statusWithoutQuotes)
    }

    def requestControllerMakeAndPublishDoMove(firstMoveCheck: Boolean, move: Move, game: GameTui) = {
        //controller.makeAndPublish(controller.doMove, firstMoveCheck, move, game)
        val newBoard = game.board match {
            case "Playing" => 0
            case "Won" => 1
            case "Lost" => 2
        }

        val url = s"http://controller:9081/controller/makeAndPublish/doMove?b=$firstMoveCheck&bombs=${game.bombs}&size=${game.side}&time=${game.time}&board=$newBoard"
        val moveRaw = move

        def moveToJson(move: Move): JsValue = {
            Json.obj("x" -> move.x, "y" -> move.y, "value" -> move.value)
        }

        val bodyField = moveToJson(moveRaw)

        val request = HttpRequest(
            method =  HttpMethods.GET,
            uri = url
        ).withEntity(HttpEntity(ContentTypes.`application/json`, bodyField.toString()))

        val bodyString = Await.result(Http().singleRequest(request).flatMap(_.entity.toStrict(5.seconds).map(_.data.utf8String)), 5.seconds)
        println(bodyString) // success

    }

    // approved - controller.makeAndPublish(controller.put, move)
    def requestControllerMakeAndPublishPut(move: Move) = {

        val url = "http://controller:9081/controller/makeAndPublish/put"
        val moveRaw = move

        def moveToJson(move: Move): JsValue = {
            Json.obj("x" -> move.x, "y" -> move.y, "value" -> move.value)
        }

        val bodyField = moveToJson(moveRaw)

        val request = HttpRequest(
            method =  HttpMethods.GET,
            uri = url
        ).withEntity(HttpEntity(ContentTypes.`application/json`, bodyField.toString()))

        val bodyString = Await.result(Http().singleRequest(request).flatMap(_.entity.toStrict(5.seconds).map(_.data.utf8String)), 5.seconds)

    }

    // approved
    def requestControllerHelpMenue= {

        val url = "http://controller:9081/controller/helpMenu"
        val request = HttpRequest(
            method =  HttpMethods.GET,
            uri = url
        )

        val bodyStringHelpMessage = Await.result(Http().singleRequest(request).flatMap(_.entity.toStrict(5.seconds).map(_.data.utf8String)), 5.seconds)
        infoMessages(">> Help Menu in TUI:", bodyStringHelpMessage) // use logging instead
    }

    // working
    def requestControllerCheat: Unit = {

        val url = "http://controller:9081/controller/cheat"

        val request = HttpRequest(
            method =  HttpMethods.GET,
            uri = url
        )

        val responseFuture: Future[HttpResponse] = Http().singleRequest(request)
        val bodyStringFuture: Future[String] = responseFuture.flatMap { response =>
            response.entity.toStrict(5.seconds).map(_.data.utf8String)
        }

        val bodyStringCheat = Await.result(bodyStringFuture, 5.seconds)
        println(bodyStringCheat) // use logging instead
    }

    // working
    def requestControllerMakeAndPublishUndo: Unit = {
        //controller.makeAndPublish(controller.undo)
        val url = "http://controller:9081/controller/makeAndPublish/undo"
        val request = HttpRequest(
            method =  HttpMethods.GET,
            uri = url
        )

        val responseFuture: Future[HttpResponse] = Http().singleRequest(request)
        val bodyStringFuture: Future[String] = responseFuture.flatMap { response =>
            response.entity.toStrict(5.seconds).map(_.data.utf8String)
        }

        val bodyStringUndo = Await.result(bodyStringFuture, 5.seconds)
        println(bodyStringUndo)
    }

    // working
    def requestControllerMakeAndPublishRedo: Unit = {
        //controller.makeAndPublish(controller.redo)
        val url = "http://controller:9081/controller/makeAndPublish/redo"
        val request = HttpRequest(
            method =  HttpMethods.GET,
            uri = url
        )

        val responseFuture: Future[HttpResponse] = Http().singleRequest(request)
        val bodyStringFuture: Future[String] = responseFuture.flatMap { response =>
            response.entity.toStrict(5.seconds).map(_.data.utf8String)
        }

        val bodyStringUndo = Await.result(bodyStringFuture, 5.seconds)
        println(bodyStringUndo) // use logging
    }

    // working
    def requestControllerSaveGame: Unit = {
        //controller.saveGame
        val url = "http://controller:9081/controller/saveGame"

        val request = HttpRequest(
            method =  HttpMethods.GET,
            uri = url
        )

        val responseFuture: Future[HttpResponse] = Http().singleRequest(request)
        val bodyStringFuture: Future[String] = responseFuture.flatMap { response =>
            response.entity.toStrict(5.seconds).map(_.data.utf8String)
        }

        val bodyStringSave = Await.result(bodyStringFuture, 5.seconds)
        println(bodyStringSave) // use logging
    }

    // working
    def requestControllerLoadGame: Unit = {
        //controller.loadGame
        val url = "http://controller:9081/controller/loadGame"

        val request = HttpRequest(
            method =  HttpMethods.GET,
            uri = url
        )

        val responseFuture: Future[HttpResponse] = Http().singleRequest(request)
        val bodyStringFuture: Future[String] = responseFuture.flatMap { response =>
            response.entity.toStrict(5.seconds).map(_.data.utf8String)
        }

        val bodyStringLoad = Await.result(bodyStringFuture, 5.seconds)
        println(bodyStringLoad) // use logging
    }

    // working
    def requestControllerFieldToString: String = {
        val url = "http://controller:9081/controller/field/toString"

        val request = HttpRequest(
            method =  HttpMethods.GET,
            uri = url
        )

        val responseFuture: Future[HttpResponse] = Http().singleRequest(request)
        val bodyStringFuture: Future[String] = responseFuture.flatMap { response =>
            response.entity.toStrict(5.seconds).map(_.data.utf8String)
        }
        val bodyStringField = Await.result(bodyStringFuture, 5.seconds)
        bodyStringField

    }

    val route: Route = {
        get {
            path("tui") {
                complete("TUI")
            } ~
              path("tui"/"hello") {
                  complete("hello")
              }
        } ~
          put {
              path("tui"/"notify") {
                  parameter("event".as[String]) { (event) =>
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


    def start(): Unit = {
        val bindFuture = Http().newServerAt("0.0.0.0", 9088).bind(route)

        bindFuture.onComplete {
            case Success(binding) =>
                println("Server online at http://localhost:9088/")
                complete(binding.toString)
            case Failure(exception) =>
                println(s"An error occurred: $exception")
                complete("fail binding")
        }
    }

    def jsonToGameAndField(jsonString: String): (GameTui, FieldTui) = {
        val json: JsValue = Json.parse(jsonString)
        val jsonGame: Option[JsValue] = (json \\ "game").headOption
        val status = (jsonGame.get \ "status").get.toString
        val bombs = (jsonGame.get \ "bombs").get.toString.toInt
        val side = (jsonGame.get \ "side").get.toString.toInt
        val time = (jsonGame.get \ "time").get.toString.toInt
        val statusWithoutQuotes = status.replace("\"", "")
        val gameTui = GameTui(bombs, side, time, statusWithoutQuotes)

        val jsonField: Option[JsValue] = (json \\ "field").headOption

        val jsonValue = jsonField.get

        val size = (jsonValue \ "size").get.toString.toInt

        val fieldVectorOption: Option[FieldTui] = Some(FieldTui(MatrixTui(Vector.tabulate(size, size) {(row, col) => "E"}), (MatrixTui(Vector.tabulate(size, size) {(row, col) => "E"}))))
        val matrixVectorOption: Option[Vector[Vector[String]]] = Some(Vector.tabulate(size, size) {(row, col) => "E"})
        val hiddenVectorOption: Option[Vector[Vector[String]]] = Some(Vector.tabulate(size, size) {(row, col) => "E"})

        val matrixVector1 = matrixVectorOption match{
            case Some(matrix) => matrix
            case None => println("Matrix is not valid"); Vector.tabulate(size, size) {(row, col) => "E"}
        }

        val updatedMatrixVector: Vector[Vector[String]] = (0 until size * size).foldLeft(matrixVector1) {
            case (currentMatrix, index) =>
                val row = (jsonValue \ "matrix" \\ "row")(index).as[Int]
                val col = (jsonValue \ "matrix" \\ "col")(index).as[Int]
                val cell = (jsonValue \ "matrix" \\ "cell")(index).as[String]
                currentMatrix.updated(row, currentMatrix(row).updated(col, cell))
        }

        val hiddenVector1 = hiddenVectorOption match{
            case Some(m) => m
            case None => println("Hidden is not valid"); Vector.tabulate(size, size) {(row, col) => "E"}
        }

        val updatedHiddenVector: Vector[Vector[String]] = (0 until size * size).foldLeft(hiddenVector1) {
            case (currentHidden, index) =>
                val row = (jsonValue \ "hidden" \\ "row")(index).as[Int]
                val col = (jsonValue \ "hidden" \\ "col")(index).as[Int]
                val cell = (jsonValue \ "hidden" \\ "cell")(index).as[String]
                currentHidden.updated(row, currentHidden(row).updated(col, cell))
        }

        val finalFieldOption = fieldVectorOption match{
            case Some(f) => Some(FieldTui(MatrixTui(updatedMatrixVector), MatrixTui(updatedHiddenVector)))
            case None => println("Field is not valid"); None
        }

        (gameTui, finalFieldOption.get)
    }

end TUI