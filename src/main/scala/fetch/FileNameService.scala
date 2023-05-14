package fetch

import cats.effect._

import java.time.format.DateTimeFormatter
import java.time.{Duration, LocalDate, LocalDateTime, ZoneId, ZonedDateTime}

object FileNameService {
  private val interval = Duration.ofMinutes(30)
  private val formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")

  def generateFromDate(date: LocalDate): List[String] = {
    val from = LocalDateTime.of(date.getYear, date.getMonth, date.getDayOfMonth, 0, 0)
    val to = LocalDateTime.of(date.getYear, date.getMonth, date.getDayOfMonth, 23, 59)
    generate(from, to)
  }

  private def roundToInterval(time: LocalDateTime, roundUp: Boolean): LocalDateTime = {
    val minutes = time.getMinute
    val adjustment = if (roundUp) (interval.toMinutes - minutes % interval.toMinutes) % interval.toMinutes
    else -minutes % interval.toMinutes

    time.plusMinutes(adjustment)
  }

  def generateCurrentHour(implicit clock: Clock[IO]): IO[String] = {
    clock.realTime.map { duration =>
      val instant = java.time.Instant.ofEpochMilli(duration.toMillis)
      val zonedDateTime = ZonedDateTime.ofInstant(instant, ZoneId.of("Europe/Riga"))
      val now = zonedDateTime.toLocalDateTime.withMinute(30)
      val csvName = s"${formatter.format(now)}.csv"
//      println("println", csvName)
      csvName
    }
  }

  // currently csv files are generated each 30 mins of hour
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