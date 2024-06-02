//  package minesweeper

//  import scala.concurrent.duration._

//  import io.gatling.core.Predef._
//  import io.gatling.http.Predef._
//  import io.gatling.jdbc.Predef._

//  class UiGuiApiTest extends Simulation {

//    private val httpProtocol = http
//      .baseUrl("http://localhost:9087")
//      .inferHtmlResources(AllowList(), DenyList(""".*\.js""", """.*\.css""", """.*\.gif""", """.*\.jpeg""", """.*\.jpg""", """.*\.ico""", """.*\.woff""", """.*\.woff2""", """.*\.(t|o)tf""", """.*\.png""", """.*\.svg""", """.*detectportal\.firefox\.com.*"""))
//      .acceptHeader("*/*")
//      .acceptEncodingHeader("gzip, deflate, br")
//      .userAgentHeader("PostmanRuntime/7.39.0")
  
//    private val headers_0 = Map("Postman-Token" -> "3ed0e07b-f662-4c9a-861e-bad7b17583e9")
  
//    private val headers_1 = Map("Postman-Token" -> "0fd3298f-f5af-4e07-a5cb-9804f8033e83")
  
//    private val headers_2 = Map("Postman-Token" -> "9a59af27-e513-48f8-b2aa-f4b44de15129")
  
//    private val headers_3 = Map("Postman-Token" -> "0109b029-ab2c-4747-bcb8-40b36e35feab")
  
//    private val headers_4 = Map("Postman-Token" -> "f27beb00-f927-4927-b353-9ea7d4ed8978")
  
//    private val headers_5 = Map("Postman-Token" -> "6fc12e33-fd13-42cb-9fc0-ce73caa67885")
  
//    private val headers_6 = Map("Postman-Token" -> "09b8bd67-91f7-4b39-be16-d5ad31359dc6")
  
//    private val headers_7 = Map("Postman-Token" -> "8122fc3c-755f-445a-9c61-d325b92527ba")
  
//    private val headers_8 = Map("Postman-Token" -> "7ee1f6c4-eaef-437c-8251-d5d78f5f792e")
  
//    private val headers_9 = Map("Postman-Token" -> "af023e88-f5ec-4801-98b2-224bf86588bd")
  
//    private val headers_10 = Map("Postman-Token" -> "080eff1d-ed84-4663-b793-84afa3705ad8")
  
//    private val headers_11 = Map("Postman-Token" -> "0d9b5e77-3548-4eec-87e1-10b54962466f")


//    private val scn = scenario("UiGuiApiTest")
//      .exec(
//        http("gui")
//          .get("/gui")
//          .headers(headers_0),
//        pause(8),
//        http("hello gui")
//          .get("/gui/hello")
//          .headers(headers_1),
//        pause(6),
//        http("notify new game") 
//          .put("/gui/notify?event=NewGame")
//          .headers(headers_2),
//        pause(12),
//        http("notify start")
//          .put("/gui/notify?event=Start")
//          .headers(headers_3),
//        pause(7),
//        http("notify next")
//          .put("/gui/notify?event=Next")
//          .headers(headers_4),
//        pause(11),
//        http("notify game over")
//          .put("/gui/notify?event=GameOver")
//          .headers(headers_5),
//        pause(7),
//        http("notify cheat")
//          .put("/gui/notify?event=Cheat")
//          .headers(headers_6),
//        pause(6),
//        http("notify help")
//          .put("/gui/notify?event=Help")
//          .headers(headers_7),
//        pause(8),
//        http("notify input")
//          .put("/gui/notify?event=Input")
//          .headers(headers_8),
//        pause(6),
//        http("notify load")
//          .put("/gui/notify?event=Load")
//          .headers(headers_9),
//        pause(5),
//        http("notify save")
//          .put("/gui/notify?event=Save")
//          .headers(headers_10),
//        pause(7),
//        http("  ")
//          .put("/gui/notify?event=SaveTime")
//          .headers(headers_11)
//      )

//  	setUp(scn.inject(atOnceUsers(1))).protocols(httpProtocol)
//  }
