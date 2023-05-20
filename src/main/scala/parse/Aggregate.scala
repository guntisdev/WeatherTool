package parse

import cats.implicits.{catsSyntaxOptionId, toFoldableOps}
import io.circe.generic.semiauto.deriveEncoder
import io.circe.syntax.EncoderOps
import io.circe.{Encoder, Json}
import java.time.LocalDateTime

object Aggregate {

  final case class UserQuery(
    cities: List[String],
    field: String,
    key: AggregateKey
  )

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
  final case class TimeDoubleList(list: List[(LocalDateTime, Option[Double])]) extends AggregateValue
  final case class StringListList(list: List[List[String]]) extends AggregateValue
  final case class DistinctStringList(list: List[String]) extends AggregateValue
  object AggregateValue {
    def getKeys: List[String] = {
      val runtimeMirror = scala.reflect.runtime.currentMirror
      val classSymbol = runtimeMirror.classSymbol(classOf[AggregateValue])

      // Get all case classes that extend the AggregateValue trait
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

  def extractDoubleFieldValues(field: String, weatherData: List[WeatherData]): List[Option[Double]] =
    field match {
      case "tempMax" => weatherData.map(_.tempMax)
      case "tempMin" => weatherData.map(_.tempMin)
      case "tempAvg" => weatherData.map(_.tempAvg)
      case "precipitation" => weatherData.map(_.precipitation)
      case "windAvg" => weatherData.map(_.windAvg)
      case "windMax" => weatherData.map(_.windMax)
      case "visibilityMin" => weatherData.map(_.visibilityMin)
      case "visibilityAvg" => weatherData.map(_.visibilityAvg)
      case "snowAvg" => weatherData.map(_.snowAvg)
      case "atmPressire" => weatherData.map(_.atmPressure)
      case "dewPoint" => weatherData.map(_.dewPoint)
      case "humidity" => weatherData.map(_.humidity)
      case "sunDuration" => weatherData.map(_.sunDuration)
//      case "phenomena" => weatherData.map(_..phenomena)
      case _ => List.empty
    }

  def aggregateDoubleValues(
    AggKey: AggregateKey,
    values: List[Option[Double]],
    timestamps: List[LocalDateTime],
  ): Option[AggregateValue] = {
    val flatValues = values.flatten
    AggKey match {
      case AggregateKey.Min => flatValues.minimumOption.map(DoubleValue)
      case AggregateKey.Max => flatValues.maximumOption.map(DoubleValue)
      case AggregateKey.Avg => flatValues.reduceOption(_ + _).map(sum => DoubleValue(sum / flatValues.length))
      case AggregateKey.Sum => flatValues.sum.some.map(DoubleValue)
      case AggregateKey.List => TimeDoubleList(timestamps.zip(values)).some
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
