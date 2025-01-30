package grib
case class Grib(
   version: Int,
   length: Long,
   grid: GribGrid,
   meteo: MeteoParam,
   time: GribTime,
   conversion: MeteoConversion,
   bitsPerDataPoint: Int,
   sections: List[GribSection],
)

case class GribGrid(
   template: Short,
   cols: Int,
   rows: Int,
)

case class MeteoParam(
  discipline: Int, // 0=meteo, 1=hydro, 2=land surface, 3=space products
  category: Int, // 0=temperature, 1=moisture, 6=cloud, 19=atmospheric
  product: Int,
  subType: String, // now or period
  levelType: Int, // 102 - entire atmosphere, 103 - above ground, above sea level
  levelValue: Int, // meters above ground/sea level
)

case class MeteoConversion(
  reference: Float, // float
  binaryScale: Int, // int
  decimalScale: Int, // int
)

case class GribSection(
  id: Int,
  offset: Long,
  size: Int,
)

case class GribTime(
  referenceTime: String,
  forecastTime: String,
)