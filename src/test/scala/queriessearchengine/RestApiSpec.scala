package queriessearchengine

import spray.http._
import spray.httpx.SprayJsonSupport._
import spray.testkit.Specs2RouteTest
import org.specs2.mutable.Specification

import Protocol._

import scala.concurrent.duration._

class RestApiSpec extends Specification with Specs2RouteTest with RestRoute {
  def actorRefFactory = system
  implicit def defaultTimeout = RouteTestTimeout(Duration(10, SECONDS))

   // Unit testing should not rely on external data, but such considerations are out of our scope here
   // The tested RestApi class tries to load the input data file indicated in the project configuration

  "The Rest service" should {
    "Return a count of queries on a range specified with a partial timestamp (year only)" in {
      Get("/1/queries/count/2015") ~> sealRoute(restRoute) ~> check {
        status mustEqual StatusCodes.OK
        responseAs[Count].count mustEqual 573697
      }
    }

    "Return a count of queries on a range specified with a partial timestamp (date only)" in {
      Get("/1/queries/count/2015-08-03") ~> sealRoute(restRoute) ~> check {
        status mustEqual StatusCodes.OK
        responseAs[Count].count mustEqual 198117
      }
    }

    "Return a count of queries on a range specified with a full timestamp" in {
      Get("/1/queries/count/2015-08-03%2006:42:00") ~> sealRoute(restRoute) ~> check {
        status mustEqual StatusCodes.OK
        responseAs[Count].count mustEqual 8
      }
    }

    "Return a count of 0 if time range contains no data" in {
      Get("/1/queries/count/2000-01-01") ~> sealRoute(restRoute) ~> check {
        status mustEqual StatusCodes.OK
        responseAs[Count].count mustEqual 0
      }
    }

    "Return an error code status if the specified range has a wrong format" in {
      Get("/1/queries/count/2000-0001-0001") ~> sealRoute(restRoute) ~> check {
        status mustEqual StatusCodes.BadRequest
      }
    }

    "Return top 5 queries on a given date range" in {
      Get("/1/queries/popular/2015-08-02?size=5") ~> sealRoute(restRoute) ~> check {
        status mustEqual StatusCodes.OK
        val queriesAndCount = responseAs[QueryAndCountList].queries
        queriesAndCount.length mustEqual 5
        queriesAndCount(0).query mustEqual "http%3A%2F%2Fwww.getsidekick.com%2Fblog%2Fbody-language-advice"
        queriesAndCount(0).count mustEqual 2283
        queriesAndCount(1).query mustEqual "http%3A%2F%2Fwebboard.yenta4.com%2Ftopic%2F568045"
        queriesAndCount(1).count mustEqual 1943
        queriesAndCount(2).query mustEqual "http%3A%2F%2Fwebboard.yenta4.com%2Ftopic%2F379035%3Fsort%3D1"
        queriesAndCount(2).count mustEqual 1358
        queriesAndCount(3).query mustEqual "http%3A%2F%2Fjamonkey.com%2F50-organizing-ideas-for-every-room-in-your-house%2F"
        queriesAndCount(3).count mustEqual 890
        queriesAndCount(4).query mustEqual "http%3A%2F%2Fsharingis.cool%2F1000-musicians-played-foo-fighters-learn-to-fly-and-it-was-epic"
        queriesAndCount(4).count mustEqual 701
      }
    }
  }
}
