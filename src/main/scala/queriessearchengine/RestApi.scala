package queriessearchengine

import akka.actor._
import spray.http.StatusCodes
import spray.routing._

import Protocol._

import scala.io.Source
import scala.util._

class RestApi extends HttpServiceActor with RestRoute {
  def receive = runRoute(restRoute)
}

trait RestRoute extends HttpService {
  private val apiVersionString = "1"
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
                  val future = queriesStore.rangedDistinctCount(dates._1, dates._2)
                  createResponder(requestContext) ! CountFuture(future)
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
                      val future = queriesStore.orderedTopRangedValues(dates._1, dates._2, size)
                      createResponder(requestContext) ! QueryAndCountListFuture(future)
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

class Responder(requestContext: RequestContext) extends Actor {
  import spray.httpx.SprayJsonSupport._
  import scala.concurrent.ExecutionContext.Implicits.global

  def receive = {
    case message: CountFuture => message.countFuture.onComplete {
      case Success(count: Int) => {
        requestContext.complete(StatusCodes.OK, Count(count))
        terminate()
      }
      case Failure(_) => {
        requestContext.complete(StatusCodes.InternalServerError)
        terminate()
      }
    }
    case message: QueryAndCountListFuture => message.listFuture.onComplete {
      case Success(seq: Seq[(String, Int)]) => {
        requestContext.complete(StatusCodes.OK, QueryAndCountList(seq.map(new QueryAndCount(_))))
        terminate()
      }
      case Failure(_) => {
        requestContext.complete(StatusCodes.InternalServerError)
        terminate()
      }
    }
    case _ => {
      requestContext.complete(StatusCodes.BadRequest)
      terminate()
    }
  }

  private def terminate() = self ! PoisonPill
}
