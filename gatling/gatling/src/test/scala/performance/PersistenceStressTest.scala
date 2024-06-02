package performance

import scala.concurrent.duration._

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._

class PersistenceStressTest extends Simulation {

  private val httpProtocol = http
    .baseUrl("http://localhost:9083")
    .inferHtmlResources(AllowList(), DenyList(""".*\.js""", """.*\.css""", """.*\.gif""", """.*\.jpeg""", """.*\.jpg""", """.*\.ico""", """.*\.woff""", """.*\.woff2""", """.*\.(t|o)tf""", """.*\.png""", """.*\.svg""", """.*detectportal\.firefox\.com.*"""))
    .acceptHeader("*/*")
    .acceptEncodingHeader("gzip, deflate, br")
    .userAgentHeader("PostmanRuntime/7.39.0")

  private val headers_0 = Map("Postman-Token" -> "a7dbe21f-87e4-4759-afea-a25086eafc54")

  private val headers_1 = Map("Postman-Token" -> "17a6defb-5d07-499e-9a9d-ed636b7114c7")

  private val headers_2 = Map("Postman-Token" -> "0e1d107c-d6e1-442e-8311-c16ec3b5012e")

  private val headers_3 = Map("Postman-Token" -> "b9602037-436d-4478-a4b0-e509345f142b")

  private val headers_4 = Map("Postman-Token" -> "6d8c7aed-52a1-41c8-8d5c-80f1846fe429")

  private val headers_5 = Map(
    "Content-Type" -> "application/json",
    "Postman-Token" -> "b4a91978-8fe5-4916-bd30-68f999817238"
  )

  private val headers_6 = Map("Postman-Token" -> "08c44585-bba7-4093-b0b7-159d409b0cfb")


  private val scn = scenario("PersistenceStressTest")
    .exec(
      http("ping")
        .get("/persistence/ping")
        .headers(headers_0),
      pause(5),
      http("get game")
        .get("/persistence/game")
        .headers(headers_1),
      pause(5),
      http("get field")
        .get("/persistence/field")
        .headers(headers_2),
      pause(5),
      http("get highscore")
        .get("/persistence/highscore")
        .headers(headers_3),
      pause(5),
      http("put game")
        .put("/persistence/putGame?bombs=10&size=9&time=0&board=Playing")
        .headers(headers_4),
      pause(5),
      http("put field")
        .put("/persistence/putField")
        .headers(headers_5)
        .body(RawFileBody("persistenceapi/0005_request.json")),
      pause(5),
      http("put highscore")
        .put("/persistence/putHighscore?player=Sly&score=777")
        .headers(headers_6)
    )

    setUp(scn.inject(stressPeakUsers(800).during(20.seconds))).protocols(httpProtocol)
}
