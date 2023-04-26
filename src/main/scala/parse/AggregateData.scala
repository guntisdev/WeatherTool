package parse

import io.circe._
import io.circe.syntax.EncoderOps

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

sealed trait AggregateValue


object AggregateValue {
  val dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd-HHmm")

  case class tempMax(value: Double) extends AggregateValue
  case class tempMaxList(value: Map[LocalDateTime, Double]) extends AggregateValue
  case class tempMin(value: Double) extends AggregateValue
  case class tempMinList(value: Map[LocalDateTime, Double]) extends AggregateValue
  case class tempAvg(value: Double) extends AggregateValue
  case class tempAvgList(value: Map[LocalDateTime, Double]) extends AggregateValue
  case class precipitationSum(value: Double) extends AggregateValue
  case class precipitationList(value: Map[LocalDateTime, Double]) extends AggregateValue
  case class windAvg(value: Double) extends AggregateValue
  case class windAvgList(value: Map[LocalDateTime, Double]) extends AggregateValue
  case class windMax(value: Double) extends AggregateValue
  case class windMaxList(value: Map[LocalDateTime, Double]) extends AggregateValue
  case class visibilityMin(value: Double) extends AggregateValue
  case class visibilityMinList(value: Map[LocalDateTime, Double]) extends AggregateValue
  case class visibilityAvg(value: Double) extends AggregateValue
  case class visibilityAvgList(value: Map[LocalDateTime, Double]) extends AggregateValue
  case class snowAvgList(value: Map[LocalDateTime, Double]) extends AggregateValue
  case class snowAvg(value: Double) extends AggregateValue
  case class snowMax(value: Double) extends AggregateValue
  case class atmPressureList(value: Map[LocalDateTime, Double]) extends AggregateValue
  case class atmPressureMin(value: Double) extends AggregateValue
  case class atmPressureMax(value: Double) extends AggregateValue
  case class atmPressureAvg(value: Double) extends AggregateValue
  case class dewList(value: Map[LocalDateTime, Double]) extends AggregateValue
  case class dewMin(value: Double) extends AggregateValue
  case class dewMax(value: Double) extends AggregateValue
  case class dewAvg(value: Double) extends AggregateValue
  case class humidityAvg(value: Double) extends AggregateValue
  case class humidityAvgList(value: Map[LocalDateTime, Double]) extends AggregateValue
  case class sunDurationList(value: Map[LocalDateTime, Double]) extends AggregateValue
  case class sunDurationSum(value: Double) extends AggregateValue

  private def encodeDateTimeMap(map: Map[LocalDateTime, Double]): Json = {
    map.map { case (dateTime, value) =>
      Json.obj(dateFormatter.format(dateTime) -> Json.fromDoubleOrNull(value))
    }.toList.asJson
  }

  implicit val encodeAggregateValue: Encoder[AggregateValue] = Encoder.instance {
    case key: tempMax => Json.fromDoubleOrNull(key.value)
    case key: tempMaxList => encodeDateTimeMap(key.value)
    case key: tempMin => Json.fromDoubleOrNull(key.value)
    case key: tempMinList => encodeDateTimeMap(key.value)
    case key: tempAvg => Json.fromDoubleOrNull(key.value)
    case key: tempAvgList => encodeDateTimeMap(key.value)
    case key: precipitationSum => Json.fromDoubleOrNull(key.value)
    case key: precipitationList => encodeDateTimeMap(key.value)
    case key: windAvg => Json.fromDoubleOrNull(key.value)
    case key: windAvgList => encodeDateTimeMap(key.value)
    case key: windMax => Json.fromDoubleOrNull(key.value)
    case key: windMaxList => encodeDateTimeMap(key.value)
    case key: visibilityMin => Json.fromDoubleOrNull(key.value)
    case key: visibilityMinList => encodeDateTimeMap(key.value)
    case key: visibilityAvg => Json.fromDoubleOrNull(key.value)
    case key: visibilityAvgList => encodeDateTimeMap(key.value)
    case key: snowAvgList => encodeDateTimeMap(key.value)
    case key: snowAvg => Json.fromDoubleOrNull(key.value)
    case key: snowMax => Json.fromDoubleOrNull(key.value)
    case key: atmPressureList => encodeDateTimeMap(key.value)
    case key: atmPressureMin => Json.fromDoubleOrNull(key.value)
    case key: atmPressureMax => Json.fromDoubleOrNull(key.value)
    case key: atmPressureAvg => Json.fromDoubleOrNull(key.value)
    case key: dewList => encodeDateTimeMap(key.value)
    case key: dewMin => Json.fromDoubleOrNull(key.value)
    case key: dewMax => Json.fromDoubleOrNull(key.value)
    case key: dewAvg => Json.fromDoubleOrNull(key.value)
    case key: humidityAvg => Json.fromDoubleOrNull(key.value)
    case key: humidityAvgList => encodeDateTimeMap(key.value)
    case key: sunDurationList => encodeDateTimeMap(key.value)
    case key: sunDurationSum => Json.fromDoubleOrNull(key.value)
  }

  def getKeys: List[String] = {
    val runtimeMirror = scala.reflect.runtime.currentMirror
    val weatherParamClassSymbol = runtimeMirror.classSymbol(classOf[AggregateValue])

    // Get all case classes that extend the WeatherParameter trait
    val weatherParameterCases = weatherParamClassSymbol.knownDirectSubclasses.map(_.name.toString).toList

    weatherParameterCases
  }
}

sealed trait AggregateKey

object AggregateKey {
  def stringToAggregateParam(strParam: String): Option[AggregateKey] = strParam match {
    case "tempMax" => Some(AggregateKey.tempMax)
    case "tempMaxList" => Some(AggregateKey.tempMaxList)
    case "tempMin" => Some(AggregateKey.tempMin)
    case "tempMinList" => Some(AggregateKey.tempMinList)
    case "tempAvg" => Some(AggregateKey.tempAvg)
    case "tempAvgList" => Some(AggregateKey.tempAvgList)
    case "precipitationSum" => Some(AggregateKey.precipitationSum)
    case "precipitationList" => Some(AggregateKey.precipitationList)
    case "windAvg" => Some(AggregateKey.windAvg)
    case "windAvgList" => Some(AggregateKey.windAvgList)
    case "windMax" => Some(AggregateKey.windMax)
    case "windMaxList" => Some(AggregateKey.windMaxList)
    case "visibilityMin" => Some(AggregateKey.visibilityMin)
    case "visibilityMinList" => Some(AggregateKey.visibilityMinList)
    case "visibilityAvg" => Some(AggregateKey.visibilityAvg)
    case "visibilityAvgList" => Some(AggregateKey.visibilityAvgList)
    case "snowAvgList" => Some(AggregateKey.snowAvgList)
    case "snowAvg" => Some(AggregateKey.snowAvg)
    case "snowMax" => Some(AggregateKey.snowMax)
    case "atmPressureList" => Some(AggregateKey.atmPressureList)
    case "atmPressureMin" => Some(AggregateKey.atmPressureMin)
    case "atmPressureMax" => Some(AggregateKey.atmPressureMax)
    case "atmPressureAvg" => Some(AggregateKey.atmPressureAvg)
    case "dewList" => Some(AggregateKey.dewList)
    case "dewMin" => Some(AggregateKey.dewMin)
    case "dewMax" => Some(AggregateKey.dewMax)
    case "dewAvg" => Some(AggregateKey.dewAvg)
    case "humidityAvg" => Some(AggregateKey.humidityAvg)
    case "humidityAvgList" => Some(AggregateKey.humidityAvgList)
    case "sunDurationList" => Some(AggregateKey.sunDurationList)
    case "sunDurationSum" => Some(AggregateKey.sunDurationSum)
    case _ => None
  }

  def getKeys: List[String] = {
    val runtimeMirror = scala.reflect.runtime.currentMirror
    val weatherParamClassSymbol = runtimeMirror.classSymbol(classOf[AggregateKey])

    // Get all case classes that extend the WeatherParameter trait
    val weatherParameterCases = weatherParamClassSymbol.knownDirectSubclasses.map(_.name.toString).toList

    weatherParameterCases
  }

  case object tempMax extends AggregateKey
  case object tempMaxList extends AggregateKey

  case object tempMin extends AggregateKey
  case object tempMinList extends  AggregateKey

  case object tempAvg extends AggregateKey
  case object tempAvgList extends AggregateKey

  case object precipitationSum extends AggregateKey
  case object precipitationList extends AggregateKey

  case object windAvg extends AggregateKey
  case object windAvgList extends AggregateKey

  case object windMax extends AggregateKey
  case object windMaxList extends AggregateKey

  case object visibilityMin extends AggregateKey
  case object visibilityMinList extends AggregateKey

  case object visibilityAvg extends AggregateKey
  case object visibilityAvgList extends AggregateKey

  case object snowAvg extends AggregateKey
  case object snowAvgList extends AggregateKey
  case object snowMax extends AggregateKey

  case object atmPressureList extends AggregateKey
  case object atmPressureMin extends AggregateKey
  case object atmPressureMax extends AggregateKey
  case object atmPressureAvg extends AggregateKey

  case object dewList extends AggregateKey
  case object dewMin extends AggregateKey
  case object dewMax extends AggregateKey
  case object dewAvg extends AggregateKey

  case object humidityAvg extends AggregateKey
  case object humidityAvgList extends AggregateKey

  case object sunDurationList extends AggregateKey
  case object sunDurationSum extends AggregateKey
}