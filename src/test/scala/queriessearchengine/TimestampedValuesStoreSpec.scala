package queriessearchengine

import org.specs2.mutable.Specification

import scala.util._

class TimestampedValuesStoreSpec extends Specification {
  "The TimestampedValuesStore" should {
    val input = Seq(
      "2015-01-01 01:00:00,query1",
      "2016-01-01 01:00:00,query1",
      "2016-01-01 01:11:00,query2",
      "2016-01-01 02:22:00,query2",
      "2016-02-01 01:00:00,query3",
      "2016-03-01 01:00:00,query3",
      "2016-03-01 01:00:00,query3",
      "2016-03-01 01:00:00,query3"
    )

    val store = new TimestampedValuesStore(input.iterator, ",")

    "Correctly count distinct values in a range" in {
      store.rangedDistinctCount("2015-01-01 00:00:00", "2015-01-01 02:00:00").onComplete {
        case Success(result) => result mustEqual 1
        case Failure(t: Throwable) => t.printStackTrace()
      }
      store.rangedDistinctCount("2016-01-01 00:00:00", "2016-02-01 00:00:00").onComplete {
        case Success(result) => result mustEqual 2
        case Failure(t: Throwable) => t.printStackTrace()
      }
      store.rangedDistinctCount("2016-01-01 00:00:00", "2015-01-01 00:00:00").onComplete {
        case Success(result) => result mustEqual 0
        case Failure(t: Throwable) => t.printStackTrace()
      }
      store.rangedDistinctCount("2017-01-01 00:00:00", "2018-01-01 00:00:00").onComplete {
        case Success(result) => result mustEqual 0
        case Failure(t: Throwable) => t.printStackTrace()
      }

      ok
    }

    "Compute accurately the top values in a range" in {
      store.orderedTopRangedValues("2016-01-01 00:00:00", "2017-01-01 00:00:00", 3).onComplete {
        case Success(result) => {
          result.length mustEqual 3
          result(0)._1 mustEqual "query3"
          result(0)._2 mustEqual 4
          result(1)._1 mustEqual "query2"
          result(1)._2 mustEqual 2
          result(2)._1 mustEqual "query1"
          result(2)._2 mustEqual 1
        }
        case Failure(t: Throwable) => {
          t.printStackTrace()
        }
      }

      store.orderedTopRangedValues("2016-03-01 00:00:00", "2017-03-01 00:00:00", 2).onComplete {
        case Success(result) => {
          result.length mustEqual 1
          result(0)._1 mustEqual "query3"
          result(0)._2 mustEqual 3
        }
        case Failure(t: Throwable) => {
          t.printStackTrace()
        }
      }

      store.orderedTopRangedValues("2017-01-01 00:00:00", "2018-01-01 00:00:00", 1).onComplete {
        case Success(result) => {
          result.length mustEqual 0
        }
        case Failure(t: Throwable) => {
          t.printStackTrace()
        }
      }

      store.orderedTopRangedValues("2015-01-01 00:00:00", "2018-01-01 00:00:00", 10).onComplete {
        case Success(result) => {
          result.length mustEqual 3
        }
        case Failure(t: Throwable) => {
          t.printStackTrace()
        }
      }

      ok
    }
  }
}
