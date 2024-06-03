package minesweeper

 import scala.concurrent.duration._

 import io.gatling.core.Predef._
 import io.gatling.http.Predef._
 import io.gatling.jdbc.Predef._

 class ModelApiTest extends Simulation {

   private val httpProtocol = http
     .baseUrl("http://192.168.178.1")
     .inferHtmlResources(AllowList(), DenyList(""".*\.js""", """.*\.css""", """.*\.gif""", """.*\.jpeg""", """.*\.jpg""", """.*\.ico""", """.*\.woff""", """.*\.woff2""", """.*\.(t|o)tf""", """.*\.png""", """.*\.svg""", """.*detectportal\.firefox\.com.*"""))
  
   private val headers_0 = Map(
   		"Accept" -> "*/*",
   		"Accept-Encoding" -> "gzip, deflate, br",
   		"Postman-Token" -> "1655c27c-f2bb-470a-83bd-20e39ed74841",
   		"User-Agent" -> "PostmanRuntime/7.39.0"
   )
  
   private val headers_1 = Map(
   		"Accept" -> "*/*",
   		"Accept-Encoding" -> "gzip, deflate, br",
   		"Postman-Token" -> "e46e0dae-ce50-4e48-9db2-90e5fbe1bd04",
   		"User-Agent" -> "PostmanRuntime/7.39.0"
   )
  
   private val headers_3 = Map(
   		"Accept" -> "*/*",
   		"Accept-Encoding" -> "gzip, deflate, br",
   		"Postman-Token" -> "9814126a-9060-43f7-9f4f-e21b6ee4bcdf",
   		"User-Agent" -> "PostmanRuntime/7.39.0"
   )
  
   private val headers_4 = Map(
   		"Accept" -> "*/*",
   		"Accept-Encoding" -> "gzip, deflate, br",
   		"Postman-Token" -> "891a2ccf-bd31-4ac6-9cc7-cfcc274f0a2b",
   		"User-Agent" -> "PostmanRuntime/7.39.0"
   )
  
   private val headers_6 = Map(
   		"Accept" -> "*/*",
   		"Accept-Encoding" -> "gzip, deflate, br",
   		"Postman-Token" -> "3b34605e-1815-4661-bb79-4e9c31c32315",
   		"User-Agent" -> "PostmanRuntime/7.39.0"
   )
  
   private val headers_7 = Map(
   		"Accept" -> "*/*",
   		"Accept-Encoding" -> "gzip, deflate, br",
   		"Postman-Token" -> "eef5eebd-93df-4115-9437-93eb334914af",
   		"User-Agent" -> "PostmanRuntime/7.39.0"
   )
  
   private val headers_9 = Map(
   		"Accept" -> "*/*",
   		"Accept-Encoding" -> "gzip, deflate, br",
   		"Postman-Token" -> "1dda5f8b-f158-45cd-badc-0757b501c3fc",
   		"User-Agent" -> "PostmanRuntime/7.39.0"
   )
  
   private val headers_11 = Map(
   		"Accept" -> "*/*",
   		"Accept-Encoding" -> "gzip, deflate, br",
   		"Postman-Token" -> "7f79b422-e2d2-46a5-aeb0-47ecbef9bc96",
   		"User-Agent" -> "PostmanRuntime/7.39.0"
   )
  
   private val headers_12 = Map(
   		"Accept" -> "*/*",
   		"Accept-Encoding" -> "gzip, deflate, br",
   		"Postman-Token" -> "5af38098-8c05-40c2-a720-29a8169b37b8",
   		"User-Agent" -> "PostmanRuntime/7.39.0"
   )
  
   private val headers_14 = Map(
   		"Accept" -> "*/*",
   		"Accept-Encoding" -> "gzip, deflate, br",
   		"Postman-Token" -> "520b9f6a-7cdb-4a39-9959-09c836aa4d48",
   		"User-Agent" -> "PostmanRuntime/7.39.0"
   )
  
   private val headers_15 = Map(
   		"Accept" -> "*/*",
   		"Accept-Encoding" -> "gzip, deflate, br",
   		"Postman-Token" -> "6e181b39-5fd7-4a48-84eb-0bf4ad29b428",
   		"User-Agent" -> "PostmanRuntime/7.39.0"
   )
  
   private val headers_17 = Map(
   		"Accept" -> "*/*",
   		"Accept-Encoding" -> "gzip, deflate, br",
   		"Postman-Token" -> "5cfa8082-8bed-4bd6-aa3f-77d07850507c",
   		"User-Agent" -> "PostmanRuntime/7.39.0"
   )
  
   private val headers_18 = Map(
   		"Accept" -> "*/*",
   		"Accept-Encoding" -> "gzip, deflate, br",
   		"Postman-Token" -> "72c081c5-e77f-4034-8ba7-f0498b490715",
   		"User-Agent" -> "PostmanRuntime/7.39.0"
   )
  
   private val headers_20 = Map(
   		"Accept" -> "*/*",
   		"Accept-Encoding" -> "gzip, deflate, br",
   		"Postman-Token" -> "bc6c9827-9603-4110-acbb-0efb5d6b0b53",
   		"User-Agent" -> "PostmanRuntime/7.39.0"
   )
  
   private val headers_21 = Map(
   		"Accept" -> "*/*",
   		"Accept-Encoding" -> "gzip, deflate, br",
   		"Postman-Token" -> "480deffd-3d5b-46f6-866f-ebcdf4d75e6f",
   		"User-Agent" -> "PostmanRuntime/7.39.0"
   )
  
   private val headers_25 = Map(
   		"Accept" -> "*/*",
   		"Accept-Encoding" -> "gzip, deflate, br",
   		"Content-Type" -> "application/json",
   		"Postman-Token" -> "2ebe23b0-2141-4209-84d2-9de4ddea8e19",
   		"User-Agent" -> "PostmanRuntime/7.39.0"
   )
  
   private val headers_29 = Map(
   		"Accept" -> "*/*",
   		"Accept-Encoding" -> "gzip, deflate, br",
   		"Content-Type" -> "application/json",
   		"Postman-Token" -> "8f3316c5-6cd2-4996-86f1-ab79c1226580",
   		"User-Agent" -> "PostmanRuntime/7.39.0"
   )
  
   private val headers_32 = Map(
   		"Accept" -> "*/*",
   		"Accept-Encoding" -> "gzip, deflate, br",
   		"Content-Type" -> "application/json",
   		"Postman-Token" -> "a36c146d-c563-4a08-acc9-56a166147b90",
   		"User-Agent" -> "PostmanRuntime/7.39.0"
   )
  
   private val headers_35 = Map(
   		"Accept" -> "*/*",
   		"Accept-Encoding" -> "gzip, deflate, br",
   		"Content-Type" -> "application/json",
   		"Postman-Token" -> "fdd71cd4-f4c6-4acb-b60b-c72e8bacfcc3",
   		"User-Agent" -> "PostmanRuntime/7.39.0"
   )
  
   private val headers_37 = Map(
   		"Accept" -> "*/*",
   		"Accept-Encoding" -> "gzip, deflate, br",
   		"Content-Type" -> "application/json",
   		"Postman-Token" -> "f68a9734-6489-4ed7-9aa6-cb6fc5962d16",
   		"User-Agent" -> "PostmanRuntime/7.39.0"
   )
  
   private val headers_40 = Map(
   		"Accept" -> "*/*",
   		"Accept-Encoding" -> "gzip, deflate, br",
   		"Content-Type" -> "application/json",
   		"Postman-Token" -> "10c8647a-199c-466d-8acf-19a5a3346d12",
   		"User-Agent" -> "PostmanRuntime/7.39.0"
   )
  
   private val headers_43 = Map(
   		"Accept" -> "*/*",
   		"Accept-Encoding" -> "gzip, deflate, br",
   		"Content-Type" -> "application/json",
   		"Postman-Token" -> "41b9e558-8519-47b2-ac36-47cfa4665581",
   		"User-Agent" -> "PostmanRuntime/7.39.0"
   )
  
   private val headers_45 = Map(
   		"Accept" -> "*/*",
   		"Accept-Encoding" -> "gzip, deflate, br",
   		"Content-Type" -> "application/json",
   		"Postman-Token" -> "1f3f2673-c5f6-4c2f-9e02-4507a8223291",
   		"User-Agent" -> "PostmanRuntime/7.39.0"
   )
  
   private val headers_47 = Map(
   		"Accept" -> "*/*",
   		"Accept-Encoding" -> "gzip, deflate, br",
   		"Content-Type" -> "application/json",
   		"Postman-Token" -> "6b80cf4e-46a0-4c28-8081-334cc0a8aa2f",
   		"User-Agent" -> "PostmanRuntime/7.39.0"
   )
  
   private val headers_50 = Map(
   		"Accept" -> "*/*",
   		"Accept-Encoding" -> "gzip, deflate, br",
   		"Content-Type" -> "application/json",
   		"Postman-Token" -> "79916bd9-11fa-4550-97bc-497c3141cb6b",
   		"User-Agent" -> "PostmanRuntime/7.39.0"
   )
  
   private val uri1 = "http://localhost:9082/model"

   private val scn = scenario("ModelApiTest")
     .exec(
       http("get game")
         .get(uri1 + "/game")
         .headers(headers_0),
       pause(3),
       http("help message")
         .get(uri1 + "/game/helpMessage")
         .headers(headers_1),
       pause(3),
       http("new game supereasy")
         .get(uri1 + "/game/new?bombs=5&size=5&time=0")
         .headers(headers_3),
       pause(3),
       http("new game easy")
         .get(uri1 + "/game/new?bombs=10&size=9&time=0")
         .headers(headers_4),
       pause(3),
       http("new game medium")
         .get(uri1 + "/game/new?bombs=40&size=15&time=0")
         .headers(headers_7),
       pause(3),
       http("new game hard")
         .get(uri1 + "/game/new?bombs=85&size=19&time=0")
         .headers(headers_9),
       pause(3),
       http("check exit playing") 
         .get(uri1 + "/game/checkExit?board=Playing")
         .headers(headers_11),
       pause(3),
       http("check exit won")
         .get(uri1 + "/game/checkExit?board=Won")
         .headers(headers_12),
       pause(6),
       http("check exit lost")
         .get(uri1 + "/game/checkExit?board=Lost")
         .headers(headers_14),
       pause(3),
       http("field new supereasy")
         .get(uri1 + "/field/new?bombs=5&size=5&time=0")
         .headers(headers_15),
       pause(3),
       http("field new easy")
         .get(uri1 + "/field/new?bombs=10&size=9&time=0")
         .headers(headers_17),
       pause(3),
       http("field new medium")
         .get(uri1 + "/field/new?bombs=40&size=15&time=0")
         .headers(headers_18),
       pause(3),
       http("field new hard")
         .get(uri1 + "/field/new?bombs=85&size=19&time=0")
         .headers(headers_20),
       pause(3),
       http("current global field in model")
         .get(uri1 + "/field")
         .headers(headers_21),
       pause(3),
       http("decider firstmove in model")
         .put(uri1 + "/field/decider?b=true&x=0&y=0&bombs=10&size=9&time=0&board=0")
         .headers(headers_25)
         .body(RawFileBody("modelapi/0025_request.json")),
       pause(3),
       http("show invisible cell of field")
         .put(uri1 + "/field/showInvisibleCell?x=0&y=0")
         .headers(headers_29)
         .body(RawFileBody("modelapi/0029_request.json")),
       pause(3),
       http("flag cell of field")
         .put(uri1 + "/field/put/flag?x=0&y=0")
         .headers(headers_32)
         .body(RawFileBody("modelapi/0032_request.json")),
       pause(3),
       http("unflag cell of field")
         .put(uri1 + "/field/put/removeFlag?x=0&y=0")
         .headers(headers_35)
         .body(RawFileBody("modelapi/0035_request.json")),
       pause(3),
       http("put symbol on cell of field")
         .put(uri1 + "/field/put?symbol=F&x=0&y=0")
         .headers(headers_37)
         .body(RawFileBody("modelapi/0037_request.json")),
       pause(3),
       http("recursive open")
         .put(uri1 + "/field/recursiveOpen?x=0&y=0")
         .headers(headers_40)
         .body(RawFileBody("modelapi/0040_request.json")),
       pause(3),
       http("game over field")
         .put(uri1 + "/field/gameOverField")
         .headers(headers_43)
         .body(RawFileBody("modelapi/0043_request.json")),
       pause(3),
       http("field toString")
         .put(uri1 + "/field/toString")
         .headers(headers_45)
         .body(RawFileBody("modelapi/0045_request.json")),
       pause(3),
       http("cheat")
         .put(uri1 + "/field/cheat")
         .headers(headers_47)
         .body(RawFileBody("modelapi/0047_request.json")),
       pause(3),
       http("new game field")
         .put(uri1 + "/game/newGameField?optionString=1")
         .headers(headers_50)
         .body(RawFileBody("modelapi/0050_request.json")),
     )

 	setUp(scn.inject(atOnceUsers(1))).protocols(httpProtocol)
 }
