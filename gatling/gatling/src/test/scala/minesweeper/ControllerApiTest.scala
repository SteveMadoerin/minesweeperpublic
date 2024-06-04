 package minesweeper

 import scala.concurrent.duration._

 import io.gatling.core.Predef._
 import io.gatling.http.Predef._
 import io.gatling.jdbc.Predef._

 class ControllerApiTest extends Simulation {

 private val httpProtocol = http
     .baseUrl("http://localhost:9081")
     .inferHtmlResources(AllowList(), DenyList(""".*\.js""", """.*\.css""", """.*\.gif""", """.*\.jpeg""", """.*\.jpg""", """.*\.ico""", """.*\.woff""", """.*\.woff2""", """.*\.(t|o)tf""", """.*\.png""", """.*\.svg""", """.*detectportal\.firefox\.com.*"""))
     .acceptHeader("*/*")
     .acceptEncodingHeader("gzip, deflate, br")
     .userAgentHeader("PostmanRuntime/7.39.0")

 private val headers_0 = Map("Postman-Token" -> "e704d0f0-0903-4202-af6a-5277c30cee85")

 private val headers_1 = Map("Postman-Token" -> "055cc16b-a3a4-4a02-a3b3-3c2c4ad367bf")

 private val headers_2 = Map("Postman-Token" -> "959e5b3c-386d-4daa-aeca-fb88b0258b10")

 private val headers_3 = Map("Postman-Token" -> "66cecd1c-e22c-4e4f-b333-3e9adab369c9")

 private val headers_4 = Map("Postman-Token" -> "303f43e9-96a7-4a65-a412-3e5cc5c8f1de")

 private val headers_5 = Map("Postman-Token" -> "3c18d7d1-3109-4547-9844-98bf56fea808")

 private val headers_6 = Map("Postman-Token" -> "d2e1a2a5-b306-4458-9ce0-c866ee9fa904")

 private val headers_7 = Map("Postman-Token" -> "1302a236-6bbb-435d-8f59-d2b4aa4b6fde")

 private val headers_8 = Map("Postman-Token" -> "7d604684-a1f9-4629-af2d-34aeb7807aef")

 private val headers_9 = Map("Postman-Token" -> "d0800ff9-caaa-4d12-ac53-6e427a061261")

 private val headers_10 = Map("Postman-Token" -> "0f40b765-305a-44fe-9c1d-25d8617fb6ba")

 private val headers_11 = Map("Postman-Token" -> "4b8f939a-1bae-4840-b019-64b0331a0a2a")

 private val headers_12 = Map(
         "Content-Type" -> "text/plain",
         "Postman-Token" -> "9eb95ead-c2c4-4647-85f0-087019e4f861"
 )

 private val headers_13 = Map(
         "Content-Type" -> "text/plain",
         "Postman-Token" -> "25487f5b-7802-4ba5-9994-c0bd9a8bf999"
 )

 private val headers_14 = Map(
         "Content-Type" -> "text/plain",
         "Postman-Token" -> "49605802-aab1-4517-bff2-7c2a135f61b5"
 )

 private val headers_15 = Map(
         "Content-Type" -> "text/plain",
         "Postman-Token" -> "c32a39c5-016c-41a2-b4f6-ae5c191797ce"
 )

 private val headers_16 = Map(
         "Content-Type" -> "text/plain",
         "Postman-Token" -> "55a00eaa-ba38-4033-8fdb-60eb3618f760"
 )

 private val headers_17 = Map(
         "Content-Type" -> "text/plain",
         "Postman-Token" -> "3b6c871e-a152-4235-b6d9-bfd4a14e3783"
 )

 private val headers_18 = Map(
         "Content-Type" -> "text/plain",
         "Postman-Token" -> "d15c3fca-9878-438c-b3cf-db21d9992985"
 )

 private val headers_19 = Map("Postman-Token" -> "a095bf1e-0e30-4e2e-84ff-fcdd53e1b2f3")

 private val headers_20 = Map("Postman-Token" -> "08426b4d-e86c-4b83-be3d-a05ae38a7232")

 private val headers_21 = Map("Postman-Token" -> "e13a257e-6edf-4667-9c82-326de95e98b1")

 private val headers_22 = Map("Postman-Token" -> "9cbba868-d320-45bf-9fbb-d63d18801cbe")

 private val headers_23 = Map("Postman-Token" -> "3291338e-4cfb-41a9-b274-c2c58e1b43de")


 private val scn = scenario("ControllerApiTest")
     .exec(
     http("ping")
         .get("/controller/")
         .headers(headers_0),
     pause(2),
     http("get field")
         .get("/controller/field")
         .headers(headers_1),
     pause(5),
     http("game over")
         .get("/controller/gameOver")
         .headers(headers_2)
         .check(status.is(500)),
     pause(12),
     http("field toString")
         .get("/controller/field/toString")
         .headers(headers_3),
     pause(4),
     http("get game")
         .get("/controller/game")
         .headers(headers_4),
     pause(3),
     http("help menu")
         .get("/controller/helpMenu")
         .headers(headers_5),
     pause(3),
     http("load game")
         .get("/controller/loadGame")
         .headers(headers_6),
     pause(10),
     http("new game gui")
         .get("/controller/newGameGui")
         .headers(headers_7),
     pause(12),
     http("save time")
         .get("/controller/saveTime?time=33")
         .headers(headers_8),
     pause(5),
     http("save score and player name")
         .get("/controller/saveScoreAndPlayerName?score=222&playerName=Gatling")
         .headers(headers_9),
     pause(7),
     http("new game field gui")
         .get("/controller/newGameFieldGui?input=Easy")
         .headers(headers_10),
     pause(9),
     http("cheat")
         .get("/controller/cheat")
         .headers(headers_11),
     pause(12),
     http("publish doMove")
         .get("/controller/makeAndPublish/doMove?b=false&bombs=10&size=9&time=2&board=0")
         .headers(headers_12)
         .body(RawFileBody("controllerapi/0012_request.txt")),
     pause(12),
     http("publish put")
         .get("/controller/makeAndPublish/put")
         .headers(headers_13)
         .body(RawFileBody("controllerapi/0013_request.txt")),
     pause(5),
     http("publish undo")
         .get("/controller/makeAndPublish/undo")
         .headers(headers_14)
         .body(RawFileBody("controllerapi/0014_request.txt")),
     pause(6),
     http("publish redo")
         .get("/controller/makeAndPublish/redo")
         .headers(headers_15)
         .body(RawFileBody("controllerapi/0015_request.txt")),
     pause(7),
     http("save game")
         .get("/controller/saveGame")
         .headers(headers_16)
         .body(RawFileBody("controllerapi/0016_request.txt")),
     pause(12),
     http("undo")
         .get("/controller/undo")
         .headers(headers_17)
         .body(RawFileBody("controllerapi/0017_request.txt")),
     pause(3),
     http("redo")
         .get("/controller/redo")
         .headers(headers_18)
         .body(RawFileBody("controllerapi/0018_request.txt")),
     pause(3),
     http("put")
         .put("/controller/put")
         .headers(headers_19),
     pause(5),
     http("check game over")
         .put("/controller/checkGameOver?board=Playing")
         .headers(headers_20),
     pause(8),
     http("check game over gui")
         .put("/controller/checkGameOverGui")
         .headers(headers_21),
     pause(8),
     http("new game")
         .put("/controller/newGame?bombs=10&side=9")
         .headers(headers_22),
     pause(9),
     http("exit")
         .put("/controller/exit")
         .headers(headers_23)
         .check(status.is(500))
     )

 setUp(scn.inject(atOnceUsers(1))).protocols(httpProtocol)
 }
