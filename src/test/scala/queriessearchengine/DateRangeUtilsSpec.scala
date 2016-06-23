package queriessearchengine

import org.specs2.mutable.Specification

class DateRangeUtilsSpec extends Specification {
  "The DateRangeUtils" should {
    "Compute the correct range" in {
      "With just a year" in {
        val result = DateRangeUtils.formattedDateRangeFromPartialDate("2016")
        result._1 mustEqual "2016-01-01 00:00:00"
        result._2 mustEqual "2017-01-01 00:00:00"
      }

      "With just a date" in {
        val result = DateRangeUtils.formattedDateRangeFromPartialDate("2016-12-12")
        result._1 mustEqual "2016-12-12 00:00:00"
        result._2 mustEqual "2016-12-13 00:00:00"
      }

      "With date and an hour" in {
        val result = DateRangeUtils.formattedDateRangeFromPartialDate("2016-06-06 12")
        result._1 mustEqual "2016-06-06 12:00:00"
        result._2 mustEqual "2016-06-06 13:00:00"
      }

      "With date and time" in {
        val result = DateRangeUtils.formattedDateRangeFromPartialDate("2016-06-06 12:12:12")
        result._1 mustEqual "2016-06-06 12:12:12"
        result._2 mustEqual "2016-06-06 12:12:13"
      }
    }

    "Correctly increment a formatted date" in {
      DateRangeUtils.incrementFormattedDateString("2016-06-06 12:12:12") mustEqual "2016-06-06 12:12:13"
    }

    "Correctly decrement a formatted date" in {
      DateRangeUtils.decrementFormattedDateString("2016-06-06 12:12:12") mustEqual "2016-06-06 12:12:11"
    }
  }
}
