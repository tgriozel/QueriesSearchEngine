package queriessearchengine

import scala.collection.immutable.SortedMap
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

// We need to get efficient value lookups for a given date (range), so the use of a Map is the obvious choice here.
// Because we deal with ranges and boundaries, using a SortedMap seems like the best choice.
// Key adjustment is delegated to the OrderedIntervalsTree custom data structure.

class TimestampedValuesStore(inputLineIterator: Iterator[String], dateValueSeparator: String) {
  private val dateToValues = inputLineIterator.map {
    _.split(dateValueSeparator) match {
      case tokens: Array[String] if tokens.length == 2 => tokens(0) -> tokens (1)
      case _ => throw new Exception("Input data is not correctly formatted")
    }
  }.toSeq.groupBy(_._1)
   .foldLeft(SortedMap.empty[String, Seq[String]]) { (treeMap, entry) =>
    val date = entry._1
    val values = entry._2.map { _._2 }
    treeMap + (date -> values)
  }
  private val firstKey = dateToValues.keySet.head
  private val lastKey = dateToValues.keySet.last
  private val intervals = new OrderedIntervalsTree[String](dateToValues.keySet.toArray)

  private def closestKeyIfOutOfRange(key: String): Option[String] = {
    if (dateToValues.isEmpty)
      throw new Exception("The data store is empty")

    key.compare(firstKey) match {
      case comp if comp <= 0 => Option(firstKey)
      case _ => {
        key.compareTo(lastKey) match {
          case result if result >= 0 => Option(key)
          case _ => None
        }
      }
    }
  }

  private def closestIncludedKey(fromKey: String): String = {
    closestKeyIfOutOfRange(fromKey) match {
      case Some(key) => key
      case None => {
        if (dateToValues.keySet.contains(fromKey))
          fromKey
        else
          intervals.correspondingInterval(fromKey)._2
      }
    }
  }

  private def rangedValuesAndCount(fromKey: String, untilKey: String): Map[String, Int] = {
    dateToValues.range(closestIncludedKey(fromKey), untilKey).values.flatten.toSeq.groupBy(identity).mapValues(_.length)
  }

  def orderedTopRangedValues(fromKey: String, untilKey: String, topCount: Int): Future[Seq[(String, Int)]] = {
    Future {
      rangedValuesAndCount(fromKey, untilKey).toSeq.sortBy(_._2).reverse.slice(0, topCount)
    }
  }

  def rangedDistinctCount(fromKey: String, untilKey: String): Future[Int] = {
    Future {
      dateToValues.range(closestIncludedKey(fromKey), untilKey).values.flatten.toSet.size
    }
  }
}
