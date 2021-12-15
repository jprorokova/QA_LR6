import scala.concurrent.duration._
import io.gatling.core.Predef._
import io.gatling.http.Predef._

object RandomGenerator {
  def randomString(length: Int): String = {
    val SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890"
    val salt = new StringBuilder
    val rnd = new scala.util.Random
    while (salt.length < length) { // length of the random string.
      val index = (rnd.nextFloat() * SALTCHARS.length).asInstanceOf[Int]
      salt.append(SALTCHARS.charAt(index))
    }
    val saltStr = salt.toString
    saltStr
  }

  def randomEmail(): String = randomString(10) + "@gmail.com"

  def randomCommentRequest() : String =
    """{"post_id":63,
      |"name":"""".stripMargin + RandomGenerator.randomString(25) + """",
      |"email":"""".stripMargin + RandomGenerator.randomEmail() + """",
      |"body":"Eveniet sunt architecto. Maxime quam ratione."}""".stripMargin
}

class Test extends Simulation {

  val httpProtocol = http
    .baseUrl("https://gorest.co.in")
    .authorizationHeader("Bearer ee59b1e2b395c186a7a92f4f4db2e0e4ea6233c2bb1d514be8af9dc348b5d88e")


  val post = scenario("Post")
    .exec(sessionPost => {
      val sessionPostUpdate = sessionPost.set("postrequest", RandomGenerator.randomCommentRequest())
      sessionPostUpdate
    })
    .exec(
      http("Post")
        .post("/public/v1/comments")
        .body(StringBody("${postrequest}")).asJson
    )
  val get = scenario("Get")
    .exec(sessionPost => {
      val sessionPostUpdate = sessionPost.set("postrequest", RandomGenerator.randomCommentRequest())
      sessionPostUpdate
    })
    .exec(
      http("Post")
        .post("/public/v1/comments")
        .body(StringBody("${postrequest}")).asJson
        .check(jsonPath("$.data.id").saveAs("commentId"))
    )
    .exitHereIfFailed
    .exec(
      http("Get")
        .get("/public/v1/comments/${commentId}")
    )

  val put = scenario("Put")
    .exec(sessionPost => {
      val sessionPostUpdate = sessionPost.set("postrequest", RandomGenerator.randomCommentRequest())
      sessionPostUpdate
    })
    .exec(
      http("Post")
        .post("/public/v1/comments")
        .body(StringBody("${postrequest}")).asJson
        .check(jsonPath("$.data.id").saveAs("commentId"))
    )
    .exitHereIfFailed
    .exec(sessionPut => {
      val sessionPutUpdate = sessionPut.set("putrequest", RandomGenerator.randomCommentRequest())
      sessionPutUpdate
    })
    .exec(
      http("Put")
        .put("/public/v1/comments/${commentId}")
        .body(StringBody("${putrequest}")).asJson
    )

  val delete = scenario("Delete")
    .exec(sessionPost => {
      val sessionPostUpdate = sessionPost.set("postrequest", RandomGenerator.randomCommentRequest())
      sessionPostUpdate
    })
    .exec(
      http("Post")
        .post("/public/v1/comments")
        .body(StringBody("${postrequest}")).asJson
        .check(jsonPath("$.data.id").saveAs("commentId"))
    )
    .exitHereIfFailed
    .exec(sessionPut => {
      val sessionPutUpdate = sessionPut.set("putrequest", RandomGenerator.randomCommentRequest())
      sessionPutUpdate
    })
    .exec(
      http("Delete")
        .delete("/public/v1/comments/${commentId}")
    )

  setUp(post.inject(rampUsers(15).during(20.seconds)).protocols(httpProtocol),
  get.inject(rampUsers(15).during(20.seconds)).protocols(httpProtocol),
  put.inject(rampUsers(15).during(20.seconds)).protocols(httpProtocol),
  delete.inject(rampUsers(15).during(20.seconds)).protocols(httpProtocol))
}