package queriessearchengine

import java.time._
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

object DateRangeUtils {
  private val FULL_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss"
  private val BASE_TIME_STRING = "0000-01-01 00:00:00"
  private val formatter = DateTimeFormatter.ofPattern(FULL_DATE_FORMAT)

  private def dateRangeFromPartialDate(partialDate: String): (LocalDateTime, LocalDateTime) = {
    val fieldToIncrement = partialDate.length match {
      case 4 => ChronoUnit.YEARS
      case 7 => ChronoUnit.MONTHS
      case 10 => ChronoUnit.DAYS
      case 13 => ChronoUnit.HOURS
      case 16 => ChronoUnit.MINUTES
      case 19 => ChronoUnit.SECONDS
      case _ => throw new Exception("The date to parse is incorrectly formatted")
    }

    val fullDateString = partialDate + BASE_TIME_STRING.substring(partialDate.length)
    val startDate = LocalDateTime.parse(fullDateString, formatter)
    val endDate = startDate.plus(1, fieldToIncrement)
    (startDate, endDate)
  }

  def formattedDateRangeFromPartialDate(partialDate: String): (String, String) = {
    val dateRange = dateRangeFromPartialDate(partialDate)
    (dateRange._1.format(formatter), dateRange._2.format(formatter))
  }
}
