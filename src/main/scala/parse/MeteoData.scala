package parse

import java.time.LocalDateTime
import scala.reflect.runtime.universe.{termNames, typeOf}


case class WeatherStationData(
                               city: String,
                               timestamp: LocalDateTime,
                               meteo: MeteoData,
                               phenomena: List[String],
                             )

case class MeteoData(
                      tempMax: Option[Double],
                      tempMin: Option[Double],
                      tempAvg: Option[Double],
                      precipitation: Option[Double],
                      windSpeedAvg: Option[Double],
                      windGustMax: Option[Double],
                      visibilityMin: Option[Double],
                      visibilityAvg: Option[Double],
                      snowThicknessAvg: Option[Double],
                      atmPressure: Option[Double],
                      dewPoint: Option[Double],
                      airHumidity: Option[Double],
                      sunshineDuration: Option[Double],
                    )

sealed trait AggregateMeteo
object AggregateMeteo {
  case object tempMax extends AggregateMeteo

  case object tempMin extends AggregateMeteo

  case object tempAvg extends AggregateMeteo

  case object precipitationSum extends AggregateMeteo

  case object windSpeedAvg extends AggregateMeteo

  case object windGustMax extends AggregateMeteo

  case object snowThicknessAvg extends AggregateMeteo

  case object dewPointAvg extends AggregateMeteo

  case object airHumidityAvg extends AggregateMeteo
}

object Meteo {
  def stringToAggregateParam(strParam: String): Option[AggregateMeteo] = strParam match {
    case "tempMax" => Some(AggregateMeteo.tempMax)
    case "tempMin" => Some(AggregateMeteo.tempMin)
    case "tempAvg" => Some(AggregateMeteo.tempAvg)
    case "precipitationSum" => Some(AggregateMeteo.precipitationSum)
    case "windSpeedAvg" => Some(AggregateMeteo.windSpeedAvg)
    case "windGustMax" => Some(AggregateMeteo.windGustMax)
    case "snowThicknessAvg" => Some(AggregateMeteo.snowThicknessAvg)
    case "dewPointAvg" => Some(AggregateMeteo.dewPointAvg)
    case "airHumidityAvg" => Some(AggregateMeteo.airHumidityAvg)
    case _ => None
  }
}

object MeteoData {
  def fromDoubles(data: List[Option[Double]]): Option[MeteoData] = data match {
    case List(
    tempMax,
    tempMin,
    tempAvg,
    precipitation,
    windSpeedAvg,
    windGustMax,
    visibilityMin,
    visibilityAvg,
    snowThicknessAvg,
    atmPressure,
    dewPoint,
    airHumidity,
    sunshineDuration
    ) => Some(MeteoData(
      tempMax,
      tempMin,
      tempAvg,
      precipitation,
      windSpeedAvg,
      windGustMax,
      visibilityMin,
      visibilityAvg,
      snowThicknessAvg,
      atmPressure,
      dewPoint,
      airHumidity,
      sunshineDuration,
    ))
    case _ => None
  }

  def getCount: Int = {
    val constructor = typeOf[MeteoData].decl(termNames.CONSTRUCTOR).asMethod
    val paramCount = constructor.paramLists.flatten.size
    paramCount
  }

  def getKeys: List[String] = {
    val constructor = typeOf[MeteoData].decl(termNames.CONSTRUCTOR).asMethod
    constructor.paramLists.flatten.map(_.name.toString)
  }
}

//val cityList: List[String] = List("Ainaži", "Alūksne", "Bauska", "Dagda", "Daugavgrīva", "Daugavpils", "Dobele", "Gulbene", "Jelgava", "Kalnciems", "Kolka", "Kuldīga", "Lielpēči", "Liepāja", "Madona", "Mērsrags", "Pāvilosta", "Piedruja", "Priekuļi", "Rēzekne", "Rīga", "Rucava", "Rūjiena", "Saldus", "Sigulda", "Sīļi", "Skrīveri", "Skulte", "Stende", "Ventspils", "Vičaki", "Zīlāni", "Zosēni", "Ainaži", "Alūksne", "Bauska", "Dagda", "Daugavgrīva", "Daugavpils", "Dobele", "Gulbene", "Jelgava", "Kalnciems", "Kolka", "Kuldīga", "Lielpēči", "Liepāja", "Madona", "Mērsrags", "Pāvilosta", "Piedruja", "Priekuļi", "Rēzekne", "Rīga", "Rucava", "Rūjiena", "Saldus", "Sigulda", "Sīļi", "Skrīveri", "Skulte", "Stende", "Ventspils", "Vičaki", "Zīlāni", "Zosēni")


//Map(
//  "tempMax" -> "Stundas maksimālā temperatūra",
//  "tempMin" -> "Stundas minimālā temperatūra",
//  "tempAvg" -> "Stundas vidējā temperatūra",
//  "precipitation" -> "Stundas nokrišņu daudzums",
//  "windSpeedAvg" -> "Vidējais vēja ātrums novērojumu termiņā (10 min. vidējais)",
//  "windGustMax" -> "Stundas maksimālās vēja brāzmas",
//  "visibilityMin" -> "Stundas minimālā redzamība",
//  "visibilityAvg" -> "Stundas vidējā redzamība",
//  "snowThicknessAvg" -> "Stundas vidējais sniega segas biezums",
//  "atmPressure" -> "Atmosfēras spiediens jūras līmenī novērojuma termiņā (milibāros)",
//  "dewPoint" -> "Rasas punkta temperatūra novērojuma termiņā",
//  "airHumidity" -> "Relatīvais gaisa mitrums novērojumu termiņā",
//  "sunshineDuration" -> "Saules spīdēšanas ilgums",
////  "weatherPhenomena" -> "Laika parādības",
//)
