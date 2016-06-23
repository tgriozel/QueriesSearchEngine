package queriessearchengine

import org.specs2.mutable.Specification

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
      store.rangedDistinctCount("2015-01-01 00:00:00", "2015-01-01 02:00:00") mustEqual 1
      store.rangedDistinctCount("2016-01-01 00:00:00", "2016-02-01 00:00:00") mustEqual 2
      store.rangedDistinctCount("2016-01-01 00:00:00", "2015-01-01 00:00:00") mustEqual 0
      store.rangedDistinctCount("2017-01-01 00:00:00", "2018-01-01 00:00:00") mustEqual 0
    }

    "Compute accurately the top values in a range" in {
      val result1 = store.orderedTopRangedValues("2016-01-01 00:00:00", "2017-01-01 00:00:00", 3)
      result1.length mustEqual 3
      result1(0)._1 mustEqual "query3"
      result1(0)._2 mustEqual 4
      result1(1)._1 mustEqual "query2"
      result1(1)._2 mustEqual 2
      result1(2)._1 mustEqual "query1"
      result1(2)._2 mustEqual 1

      val result2 = store.orderedTopRangedValues("2016-03-01 00:00:00", "2017-03-01 00:00:00", 2)
      result2.length mustEqual 1
      result2(0)._1 mustEqual "query3"
      result2(0)._2 mustEqual 3

      store.orderedTopRangedValues("2017-01-01 00:00:00", "2018-01-01 00:00:00", 1).length mustEqual 0

      store.orderedTopRangedValues("2015-01-01 00:00:00", "2018-01-01 00:00:00", 10).length mustEqual 3
    }
  }
}
