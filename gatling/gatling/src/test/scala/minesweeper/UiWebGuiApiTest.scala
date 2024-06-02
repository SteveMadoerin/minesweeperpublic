//  package minesweeper

//  import scala.concurrent.duration._

//  import io.gatling.core.Predef._
//  import io.gatling.http.Predef._
//  import io.gatling.jdbc.Predef._

//  class UiWebGuiApiTest extends Simulation {

//    private val httpProtocol = http
//      .baseUrl("http://localhost:9080")
//      .inferHtmlResources(AllowList(), DenyList(""".*\.js""", """.*\.css""", """.*\.gif""", """.*\.jpeg""", """.*\.jpg""", """.*\.ico""", """.*\.woff""", """.*\.woff2""", """.*\.(t|o)tf""", """.*\.png""", """.*\.svg""", """.*detectportal\.firefox\.com.*"""))
//      .acceptHeader("*/*")
//      .acceptEncodingHeader("gzip, deflate, br")
//      .userAgentHeader("PostmanRuntime/7.39.0")
  
//    private val headers_0 = Map("Postman-Token" -> "a391e13c-87e5-4a76-82c2-07d2270c268b")
  
//    private val headers_1 = Map("Postman-Token" -> "8570c98b-d8f2-43c1-a318-9d1b6f2c8471")
  
//    private val headers_2 = Map("Postman-Token" -> "ff59a750-c4dd-4a38-af58-a84077a9ea3f")
  
//    private val headers_3 = Map("Postman-Token" -> "1d723404-f3e2-40cf-88ce-e45f21d618eb")
  
//    private val headers_4 = Map("Postman-Token" -> "e0f187bd-b08d-45bb-9832-77d93725b640")
  
//    private val headers_5 = Map("Postman-Token" -> "ea52f658-bc47-4576-bd81-0acb034f76a4")
  
//    private val headers_6 = Map("Postman-Token" -> "97679e49-b899-488e-9b15-0b5d492c8d5b")
  
//    private val headers_7 = Map("Postman-Token" -> "74ae0d0d-70ce-4265-a0e0-ccc3e3f7bae1")
  
//    private val headers_8 = Map("Postman-Token" -> "15cd2a56-3e5d-43c8-8040-668ee7bb8ec2")
  
//    private val headers_9 = Map("Postman-Token" -> "dcf2622d-2dce-4c0b-a4ac-4f552edee75e")
  
//    private val headers_10 = Map("Postman-Token" -> "024062e7-525f-42f1-b663-603268b34c56")
  
//    private val headers_11 = Map("Postman-Token" -> "b98dd4a9-ad5e-421b-a660-7f85074255e2")
  
//    private val headers_12 = Map("Postman-Token" -> "04567894-c2f1-4971-9d40-b6b28e635bdd")


//    private val scn = scenario("UiWebGuiApiTest")
//      .exec(
//        http("Welcome Minesweeper") 
//          .get("/ui")
//          .headers(headers_0),
//        pause(8),
//        http("new game field small")
//          .get("/ui/new/small")
//          .headers(headers_1),
//        pause(7),
//        http("undo")
//          .get("/ui/undo")
//          .headers(headers_2),
//        pause(6),
//        http("redo")
//          .get("/ui/redo")
//          .headers(headers_3),
//        pause(7),
//        http("interactive o0808")
//          .get("/minesweeper/o0808")
//          .headers(headers_4),
//        pause(5),
//        http("interactive f0000")
//          .get("/minesweeper/f0000")
//          .headers(headers_5),
//        pause(5),
//        http("interactive h")
//          .get("/minesweeper/h")
//          .headers(headers_6),
//        pause(8),
//        http("interactive r")
//          .get("/minesweeper/r")
//          .headers(headers_7),
//        pause(11),
//        http("event new game")
//          .put("/putEvent?event=NewGame")
//          .headers(headers_8),
//        pause(4),
//        http("event start")
//          .put("/putEvent?event=Start")
//          .headers(headers_9),
//        pause(5),
//        http("event next")
//          .put("/putEvent?event=Next")
//          .headers(headers_10),
//        pause(4),
//        http("event load")
//          .put("/putEvent?event=Load")
//          .headers(headers_11),
//        pause(5),
//        http("event game over")
//          .put("/putEvent?event=GameOver")
//          .headers(headers_12)
//      )

//  	setUp(scn.inject(atOnceUsers(1))).protocols(httpProtocol)
//  }
