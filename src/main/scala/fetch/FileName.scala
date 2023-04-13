package fetch

import java.time.format.DateTimeFormatter
import java.time.{LocalDateTime, Duration}

object FileName {
  private val interval = Duration.ofMinutes(30)
  private val formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")

  def generateLastHour(): List[String] = {
    val now = LocalDateTime.now
    generate(now.minusHours(1), now)
  }

  // TODO side effect
  def generateLastNHours(hours: Int, now: LocalDateTime = LocalDateTime.now): List[String] = {
    val roundNow = roundToInterval(now, false)
    generate(roundNow.minusHours(hours), roundNow)
  }

  private def roundToInterval(time: LocalDateTime, roundUp: Boolean): LocalDateTime = {
    val minutes = time.getMinute
    val adjustment = if (roundUp) (interval.toMinutes - minutes % interval.toMinutes) % interval.toMinutes
    else -minutes % interval.toMinutes

    time.plusMinutes(adjustment)
  }

  // not sure either data are each 10 minutes or each 30 mins of hour
  def generate(startTime: LocalDateTime, endTime: LocalDateTime): List[String] = {
    val roundStartTime = roundToInterval(startTime, true)
    val roundEndTime = roundToInterval(endTime, false)

    Iterator.iterate(roundStartTime) { time =>
      time.plus(interval)
    }.takeWhile(!_.isAfter(roundEndTime))
      .filter(_.getMinute == 30) // current server accepts only 2:30 3:30 4:30, etc. Later will be available each 10min or even 1min
      .map(time => s"${formatter.format(time)}.csv")
      .toList
  }
}