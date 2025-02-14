package fetch.dmi

import java.time.{ZoneOffset, ZonedDateTime}

object FileName {
  def main(args: Array[String]): Unit = {
    println("-------- FileName")

    val nowUTC = ZonedDateTime.now(ZoneOffset.UTC)
    val referenceTime = getClosestReferenceTime(nowUTC)
    val timeList = generateTimeList(referenceTime)

    timeList.foreach(println)

  }

  def getClosestReferenceTime(time: ZonedDateTime): ZonedDateTime = {
    val referenceHours = Vector(0, 3, 6, 9, 12, 15, 18, 21)
    val currentHour = time.getHour
    val closestHour = referenceHours
      .filter(h => h <= currentHour)
      .maxOption
      .getOrElse(21) // actually should not be such case

    time
      .withHour(closestHour)
      .withMinute(0)
      .withSecond(0)
      .withNano(0)
  }

  // TODO make default count to 61
  def generateTimeList(initialTime: ZonedDateTime, interval: Int = 1, count: Int = 5): List[ZonedDateTime] = {
    (0 until count).map(id => initialTime.plusHours(id * interval)).toList
  }
}
