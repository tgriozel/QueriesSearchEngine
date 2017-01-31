package queriessearchengine

import org.specs2.mutable.Specification

class OrderedIntervalsTreeSpec extends Specification {
  "The OrderedIntervalsTree should" in {
    val input = Array(
      "2015-01-01 01:00:00",
      "2016-01-01 01:00:00",
      "2016-01-01 01:11:00",
      "2016-01-01 02:22:00",
      "2016-02-01 01:00:00",
      "2016-03-01 01:00:00",
      "2016-03-01 01:00:00",
      "2016-03-01 01:00:00"
    )
    val intervals = new OrderedIntervalsTree[String](input)

    "Return the appropriate matching ranges" in {
      val r0 = intervals.correspondingInterval("2017")
      r0._1 mustEqual "2016-03-01 01:00:00"
      r0._2 mustEqual "2016-03-01 01:00:00"

      val r1 = intervals.correspondingInterval("2015")
      r1._1 mustEqual "2015-01-01 01:00:00"
      r1._2 mustEqual "2015-01-01 01:00:00"

      val r2 = intervals.correspondingInterval("2015-01-01 01:00:00")
      r2._1 mustEqual "2015-01-01 01:00:00"
      r2._2 mustEqual "2015-01-01 01:00:00"

      val r3 = intervals.correspondingInterval("2015-10-10 01:00:00")
      r3._1 mustEqual "2015-01-01 01:00:00"
      r3._2 mustEqual "2016-01-01 01:00:00"

      val r4 = intervals.correspondingInterval("2016-02-01 01:00:01")
      r4._1 mustEqual "2016-02-01 01:00:00"
      r4._2 mustEqual "2016-03-01 01:00:00"
    }
  }
}
