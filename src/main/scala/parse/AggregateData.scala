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

  implicit val encodeAggregateValue: Encoder[AggregateValue] = Encoder.instance {
    case tm: tempMax => Json.fromDoubleOrNull(tm.value)
    case tml: tempMaxList => tml.value.map { case (dateTime, value) =>
      Json.obj(dateFormatter.format(dateTime) -> Json.fromDoubleOrNull(value))
    }.toList.asJson
    case tmin: tempMin => Json.fromDoubleOrNull(tmin.value)
    case tml: tempMinList => tml.value.map { case (dateTime, value) =>
      Json.obj(dateFormatter.format(dateTime) -> Json.fromDoubleOrNull(value))
    }.toList.asJson
    case tavg: tempAvg => Json.fromDoubleOrNull(tavg.value)
    case tml: tempAvgList => tml.value.map { case (dateTime, value) =>
      Json.obj(dateFormatter.format(dateTime) -> Json.fromDoubleOrNull(value))
    }.toList.asJson
    case psum: precipitationSum => Json.fromDoubleOrNull(psum.value)
    case plist: precipitationList => plist.value.map { case (dateTime, value) =>
      Json.obj(dateFormatter.format(dateTime) -> Json.fromDoubleOrNull(value))
    }.toList.asJson
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
    //    case "windSpeedAvg" => Some(AggregateKey.windSpeedAvg)
    //    case "windGustMax" => Some(AggregateKey.windGustMax)
    //    case "snowThicknessAvg" => Some(AggregateKey.snowThicknessAvg)
    //    case "dewPointAvg" => Some(AggregateKey.dewPointAvg)
    //    case "airHumidityAvg" => Some(AggregateKey.airHumidityAvg)
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

  //  case object windSpeedAvg extends AggregateKey
  //
  //  case object windGustMax extends AggregateKey
  //
  //  case object snowThicknessAvg extends AggregateKey
  //
  //  case object dewPointAvg extends AggregateKey
  //
  //  case object airHumidityAvg extends AggregateKey
}