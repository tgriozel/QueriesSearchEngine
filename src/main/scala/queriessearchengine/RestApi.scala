package queriessearchengine

import akka.actor._
import spray.http.StatusCodes
import spray.httpx.SprayJsonSupport._
import spray.routing._
import Protocol._

import scala.io.Source
import scala.util._

class RestApi extends HttpServiceActor with RestRoute {
  def receive = runRoute(restRoute)
}

trait RestRoute extends HttpService {
  val apiVersionString = "1"
  private val file = Source.fromFile(com.typesafe.config.ConfigFactory.load.getString("input.tsv-file"))
  private val queriesStore = new TimestampedValuesStore(file.getLines(), "\t")

  private val actorSystem = ActorSystem("queriessearchengine")
  private implicit val executionContext = actorSystem.dispatcher


  val restRoute = {
    pathPrefix(apiVersionString) {
      pathPrefix("queries") {
        pathPrefix("count" / Segment) { partialDate =>
          pathEnd {
            get { requestContext =>
              Try(DateRangeUtils.formattedDateRangeFromPartialDate(partialDate)) match {
                case Success(dates) => {
                  val result = queriesStore.rangedDistinctCount(dates._1, dates._2)
                  createResponder(requestContext) ! Count(result)
                }
                case Failure(throwable) => {
                  createResponder(requestContext) ! throwable
                }
              }
            }
          }
        } ~
          pathPrefix("popular" / Segment) { partialDate =>
            pathEnd {
              get {
                parameters('size.as[Int]) { size => { requestContext =>
                  Try(DateRangeUtils.formattedDateRangeFromPartialDate(partialDate)) match {
                    case Success(dates) => {
                      val result = queriesStore.orderedTopRangedValues(dates._1, dates._2, size)
                      createResponder(requestContext) ! QueryAndCountList(result.map{ new QueryAndCount(_)} )
                    }
                    case Failure(throwable) => {
                      createResponder(requestContext) ! throwable
                    }
                  }
                }}
              }
            }
          }
      }
    }
  }

  private def createResponder(requestContext: RequestContext) = {
    actorSystem.actorOf(Props(new Responder(requestContext)))
  }
}

class Responder(requestContext: RequestContext) extends Actor with ActorLogging {
  def receive = {
    case count: Count => requestContext.complete(StatusCodes.OK, count)
    case queryAndCountList: QueryAndCountList => requestContext.complete(StatusCodes.OK, queryAndCountList)
    case _ => requestContext.complete(StatusCodes.BadRequest)

    terminate()
  }

  private def terminate() = self ! PoisonPill
}
