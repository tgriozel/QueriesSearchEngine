package queriessearchengine

import spray.json.DefaultJsonProtocol._

import scala.concurrent.Future

object Protocol {
  // Model
  case class Count(count: Int)
  case class QueryAndCount(query: String, count: Int) { def this(entry: (String, Int)) = this(entry._1, entry._2) }
  case class QueryAndCountList(queries: Seq[QueryAndCount])

  // Futures
  case class CountFuture(countFuture: Future[Int])
  case class QueryAndCountListFuture(listFuture: Future[Seq[(String, Int)]])

  // Marshallers
  object Count { implicit val format = jsonFormat1(Count.apply) }
  object QueryAndCount { implicit val format = jsonFormat2(QueryAndCount.apply) }
  object QueryAndCountList { implicit val format = jsonFormat1(QueryAndCountList.apply) }
}
