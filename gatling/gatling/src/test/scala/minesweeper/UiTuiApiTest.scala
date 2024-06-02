package minesweeper

 import scala.concurrent.duration._

 import io.gatling.core.Predef._
 import io.gatling.http.Predef._
 import io.gatling.jdbc.Predef._

 class UiTuiApiTest extends Simulation {

   private val httpProtocol = http
     .baseUrl("http://localhost:9088")
     .inferHtmlResources(AllowList(), DenyList(""".*\.js""", """.*\.css""", """.*\.gif""", """.*\.jpeg""", """.*\.jpg""", """.*\.ico""", """.*\.woff""", """.*\.woff2""", """.*\.(t|o)tf""", """.*\.png""", """.*\.svg""", """.*detectportal\.firefox\.com.*"""))
     .acceptHeader("*/*")
     .acceptEncodingHeader("gzip, deflate, br")
     .userAgentHeader("PostmanRuntime/7.39.0")
  
   private val headers_0 = Map("Postman-Token" -> "a378bac0-fd64-41c7-a5c9-8d138aa14afb")
  
   private val headers_1 = Map("Postman-Token" -> "230b191b-795e-4c88-b664-8fb3de3cc33b")
  
   private val headers_2 = Map("Postman-Token" -> "2df1a442-3ec2-49c6-9adb-3b5ca4d6f2dc")
  
   private val headers_3 = Map("Postman-Token" -> "9e459c08-9ab8-4073-a212-62d741835284")
  
   private val headers_4 = Map("Postman-Token" -> "38fbbb1e-00d0-490a-a215-32d71e989b7d")
  
   private val headers_5 = Map("Postman-Token" -> "5990536b-05fe-4127-91db-60e70bfc7b0d")
  
   private val headers_6 = Map("Postman-Token" -> "44df8545-1788-4c1a-af84-8e73d0d60e56")
  
   private val headers_7 = Map("Postman-Token" -> "55e7563a-9112-477e-9bc3-099b771ac2b4")
  
   private val headers_8 = Map("Postman-Token" -> "967d02ca-5eeb-42b4-adfa-6c21e9384f32")
  
   private val headers_9 = Map("Postman-Token" -> "01e43b9f-5c35-41b2-982b-3381cafd478f")
  
   private val headers_10 = Map("Postman-Token" -> "c472c9d1-df81-4862-b213-aed0061e49f8")


   private val scn = scenario("UiTuiApiTest")
     .exec(
       http("tui")
         .get("/tui")
         .headers(headers_0),
       pause(7),
       http("hello tui")
         .get("/tui/hello")
         .headers(headers_1),
       pause(12),
       http("notify new game")
         .put("/tui/notify?event=NewGame")
         .headers(headers_2),
       pause(8),
       http("notify start")
         .put("/tui/notify?event=Start")
         .headers(headers_3),
       pause(5),
       http("notify next")
         .put("/tui/notify?event=Next")
         .headers(headers_4),
       pause(5),
       http("notify game over")
         .put("/tui/notify?event=GameOver")
         .headers(headers_5),
       pause(5),
       http("notify cheat")
         .put("/tui/notify?event=Cheat")
         .headers(headers_6),
       pause(5),
       http("notify help")
         .put("/tui/notify?event=Help")
         .headers(headers_7),
       pause(5),
       http("notify input")
         .put("/tui/notify?event=Input")
         .headers(headers_8),
       pause(5),
       http("notify load")
         .put("/tui/notify?event=Load")
         .headers(headers_9),
       pause(5),
       http("notify save")
         .put("/tui/notify?event=SaveTime")
         .headers(headers_10)
     )

 	setUp(scn.inject(atOnceUsers(20))).protocols(httpProtocol)
 }
