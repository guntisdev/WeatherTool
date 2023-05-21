package parse

import java.time.LocalDateTime
import scala.reflect.runtime.universe._


final case class WeatherStationData(
                               city: String,
                               timestamp: LocalDateTime,
                               weather: WeatherData,
                             )

final case class WeatherData(
                      tempMax: Option[Double],
                      tempMin: Option[Double],
                      tempAvg: Option[Double],
                      precipitation: Option[Double],
                      windAvg: Option[Double],
                      windMax: Option[Double],
                      visibilityMin: Option[Double],
                      visibilityAvg: Option[Double],
                      snowAvg: Option[Double],
                      atmPressure: Option[Double],
                      dewPoint: Option[Double],
                      humidity: Option[Double],
                      sunDuration: Option[Double],
                      phenomena: List[String],
                    )

object WeatherData {
  def fromDoubles(data: List[Option[Double]], phenomena: List[String]): Option[WeatherData] = data match {
    case List(
    tempMax,
    tempMin,
    tempAvg,
    precipitation,
    windAvg,
    windMax,
    visibilityMin,
    visibilityAvg,
    snowAvg,
    atmPressure,
    dewPoint,
    humidity,
    sunDuration
    ) => Some(WeatherData(
      tempMax,
      tempMin,
      tempAvg,
      precipitation,
      windAvg,
      windMax,
      visibilityMin,
      visibilityAvg,
      snowAvg,
      atmPressure,
      dewPoint,
      humidity,
      sunDuration,
      phenomena,
    ))
    case _ => None
  }

  def getParamCount: Int = {
    val constructor = typeOf[WeatherData].decl(termNames.CONSTRUCTOR).asMethod
    val paramCount = constructor.paramLists.flatten.size
    paramCount
  }

  def getDoubleParamCount: Int = {
    getParamCount - 1 // minus one because 'phenomena' List[String] not Double
  }

  def getKeys: List[String] = {
    WeatherData(None, None, None, None, None, None, None, None, None, None, None, None, None, List.empty).productElementNames.toList
  }
}

//val cityList: List[String] = List("Ainaži", "Alūksne", "Bauska", "Dagda", "Daugavgrīva", "Daugavpils", "Dobele", "Gulbene", "Jelgava", "Kalnciems", "Kolka", "Kuldīga", "Lielpēči", "Liepāja", "Madona", "Mērsrags", "Pāvilosta", "Piedruja", "Priekuļi", "Rēzekne", "Rīga", "Rucava", "Rūjiena", "Saldus", "Sigulda", "Sīļi", "Skrīveri", "Skulte", "Stende", "Ventspils", "Vičaki", "Zīlāni", "Zosēni")


//Map(
//  "tempMax" -> "Stundas maksimālā temperatūra",
//  "tempMin" -> "Stundas minimālā temperatūra",
//  "tempAvg" -> "Stundas vidējā temperatūra",
//  "precipitation" -> "Stundas nokrišņu daudzums",
//  "windAvg" -> "Vidējais vēja ātrums novērojumu termiņā (10 min. vidējais)",
//  "windMax" -> "Stundas maksimālās vēja brāzmas",
//  "visibilityMin" -> "Stundas minimālā redzamība",
//  "visibilityAvg" -> "Stundas vidējā redzamība",
//  "snowAvg" -> "Stundas vidējais sniega segas biezums",
//  "atmPressure" -> "Atmosfēras spiediens jūras līmenī novērojuma termiņā (milibāros)",
//  "dewPoint" -> "Rasas punkta temperatūra novērojuma termiņā",
//  "humidity" -> "Relatīvais gaisa mitrums novērojumu termiņā",
//  "sunDuration" -> "Saules spīdēšanas ilgums",
////  "phenomena" -> "Laika parādības",
//)
