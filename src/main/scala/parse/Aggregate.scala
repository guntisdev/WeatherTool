package parse

import cats.data.NonEmptyList
import cats.implicits.{catsSyntaxOptionId, toFoldableOps}
import io.circe.generic.semiauto.deriveEncoder
import io.circe.syntax.EncoderOps
import io.circe.{Encoder, Json}

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

object Aggregate {

  final case class UserQuery(
                              cities: NonEmptyList[String],
                              field: String,
                              key: AggregateKey,
                              granularity: ChronoUnit,
                              from: LocalDateTime,
                              to: LocalDateTime,
  )

  implicit val userQueryEncoder: Encoder[UserQuery] = new Encoder[UserQuery] {
    override def apply(userQuery: UserQuery): Json = Json.obj(
      "cities" -> Json.fromValues(userQuery.cities.toList.map(Json.fromString)),
      "field" -> Json.fromString(userQuery.field),
      "key" -> Json.fromString(userQuery.key.toString),
      "granularity" -> Json.fromString(userQuery.granularity.toString)
    )
  }

  sealed trait AggregateKey
  object AggregateKey {
    case object Min extends AggregateKey
    case object Max extends AggregateKey
    case object Avg extends AggregateKey
    case object Sum extends AggregateKey
    case object List extends AggregateKey
    case object Distinct extends AggregateKey

    def fromString(str: String): Option[AggregateKey] = str match {
      case "min" => Some(AggregateKey.Min)
      case "max" => Some(AggregateKey.Max)
      case "avg" => Some(AggregateKey.Avg)
      case "sum" => Some(AggregateKey.Sum)
      case "list" => Some(AggregateKey.List)
      case "distinct" => Some(AggregateKey.Distinct)
      case _ => None
    }

    def getKeys: List[String] = {
      val runtimeMirror = scala.reflect.runtime.currentMirror
      val classSymbol = runtimeMirror.classSymbol(classOf[AggregateKey])

      // Get all case classes that extend the AggregateKey trait
      val keys = classSymbol.knownDirectSubclasses.map(_.name.toString.toLowerCase).toList

      keys
    }
  }

  sealed trait AggregateValue
  final case class DoubleValue(value: Double) extends AggregateValue
  final case class TimeDoubleList(list: List[(String, Option[Double])]) extends AggregateValue
  final case class StringListList(list: List[List[String]]) extends AggregateValue
  final case class DistinctStringList(list: List[String]) extends AggregateValue
  object AggregateValue {
    def getKeys: List[String] = {
      val runtimeMirror = scala.reflect.runtime.currentMirror
      val classSymbol = runtimeMirror.classSymbol(classOf[AggregateValue])
      val keys = classSymbol.knownDirectSubclasses.map(_.name.toString).toList

      keys
    }
  }

  object AggregateValueImplicits {
    implicit val aggregateValueEncoder: Encoder[AggregateValue] = Encoder.instance {
      case doubleValue: DoubleValue => doubleValue.asJson
      case timeDoubleList: TimeDoubleList => timeDoubleList.asJson
      case stringListList: StringListList => stringListList.asJson
      case distinctStringList: DistinctStringList => distinctStringList.asJson
    }

    implicit val doubleValueEncoder: Encoder[DoubleValue] = Encoder.instance {
      case DoubleValue(value) if !value.isNaN => Json.fromDouble(value).get
      case _ => Json.Null
    }
    implicit val listOptionValueEncoder: Encoder[TimeDoubleList] = Encoder.instance { loValue =>
      Json.arr(loValue.list.map(_.asJson): _*)
    }
    implicit val stringListListEncoder: Encoder[StringListList] = Encoder.instance { sllValue =>
      Json.arr(sllValue.list.map(_.asJson): _*)
    }
    implicit val distinctStringListEncoder: Encoder[DistinctStringList] = deriveEncoder[DistinctStringList]
  }

  private def convertDateTime(granularity: ChronoUnit)(data: (LocalDateTime, _)): String = {
    val dateTime = data._1
    granularity match {
      case ChronoUnit.DAYS => dateTime.toLocalDate.toString
      case ChronoUnit.MONTHS => dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM"))
      case ChronoUnit.YEARS => dateTime.format(DateTimeFormatter.ofPattern("yyyy"))
      case _ => dateTime.withMinute(0).toString
    }
  }

  private def groupByGranularity(
     list: List[(LocalDateTime, Option[Double])],
     granularity: ChronoUnit,
     aggregateKey: AggregateKey,
  ): List[(String, Option[Double])] = {
    list
      .groupBy(convertDateTime(granularity))
      .toList
      .map { case(dateTime, groupedList) => (dateTime, flattenField(aggregateKey, groupedList)) }
  }

  def aggregateByField(
    field: String,
    granularity: ChronoUnit,
    data: List[WeatherStationData],
  ): List[(String, Option[Double])] =
    field match {
      case "tempMax" => groupByGranularity(data.map(d => (d.timestamp, d.weather.tempMax)), granularity, AggregateKey.Max)
      case "tempMin" => groupByGranularity(data.map(d => (d.timestamp, d.weather.tempMin)), granularity, AggregateKey.Min)
      case "tempAvg" => groupByGranularity(data.map(d => (d.timestamp, d.weather.tempAvg)), granularity, AggregateKey.Avg)
      case "precipitation" => groupByGranularity(data.map(d => (d.timestamp, d.weather.precipitation)), granularity, AggregateKey.Sum)
      case "windAvg" => groupByGranularity(data.map(d => (d.timestamp, d.weather.windAvg)), granularity, AggregateKey.Avg)
      case "windMax" => groupByGranularity(data.map(d => (d.timestamp, d.weather.windMax)), granularity, AggregateKey.Max)
      case "visibilityMin" => groupByGranularity(data.map(d => (d.timestamp, d.weather.visibilityMin)), granularity, AggregateKey.Min)
      case "visibilityAvg" => groupByGranularity(data.map(d => (d.timestamp, d.weather.visibilityAvg)), granularity, AggregateKey.Avg)
      case "snowAvg" => groupByGranularity(data.map(d => (d.timestamp, d.weather.snowAvg)), granularity, AggregateKey.Avg)
      case "atmPressure" => groupByGranularity(data.map(d => (d.timestamp, d.weather.atmPressure)), granularity, AggregateKey.Avg)
      case "dewPoint" => groupByGranularity(data.map(d => (d.timestamp, d.weather.dewPoint)), granularity, AggregateKey.Avg)
      case "humidity" => groupByGranularity(data.map(d => (d.timestamp, d.weather.humidity)), granularity, AggregateKey.Avg)
      case "sunDuration" => groupByGranularity(data.map(d => (d.timestamp, d.weather.sunDuration)), granularity, AggregateKey.Sum)
      case _ => List.empty
    }

  implicit class RoundDecimal(val d: Double) extends AnyVal {
    def roundDecimal: Double = BigDecimal(d).setScale(1, BigDecimal.RoundingMode.HALF_UP).toDouble
  }

  private def flattenField(
   AggKey: AggregateKey,
   values: List[(LocalDateTime, Option[Double])],
  ): Option[Double] = {
    val flatValues = values.flatMap(_._2)
    AggKey match {
      case AggregateKey.Min => flatValues.minimumOption
      case AggregateKey.Max => flatValues.maximumOption
      case AggregateKey.Avg => flatValues.reduceOption(_ + _).map(_ / flatValues.length)
      case AggregateKey.Sum => flatValues.sum.some
      case _ => None
    }
  }

  def aggregateDoubleValues(
    AggKey: AggregateKey,
    values: List[(String, Option[Double])],
  ): Option[AggregateValue] = {
    val flatValues = values.flatMap(_._2)
    AggKey match {
      case AggregateKey.Min => flatValues.minimumOption.map(value => DoubleValue(value.roundDecimal))
      case AggregateKey.Max => flatValues.maximumOption.map(value => DoubleValue(value.roundDecimal))
      case AggregateKey.Avg => flatValues.reduceOption(_ + _).map(sum => DoubleValue((sum / flatValues.length).roundDecimal))
      case AggregateKey.Sum => flatValues.sum.some.map(value => DoubleValue(value.roundDecimal))
      case AggregateKey.List => TimeDoubleList(values.sorted).some
      case AggregateKey.Distinct => None
    }
  }

  def aggregatePhenomenaValues(
     aggregationType: AggregateKey,
     values: List[List[String]]
  ): Option[AggregateValue] =
    aggregationType match {
      case AggregateKey.List => StringListList(values).some
      case AggregateKey.Distinct => DistinctStringList(values.flatten.distinct).some
      case _ => None
    }
}
