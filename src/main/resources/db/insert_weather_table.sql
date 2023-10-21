INSERT INTO weather (dateTime, city, tempMax, tempMin, tempAvg, precipitation, windAvg, windMax, visibilityMin, visibilityAvg, snowAvg, atmPressure, dewPoint, humidity, sunDuration, phenomena)
    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    ON CONFLICT (city, dateTime)
    DO UPDATE SET
        tempMax = excluded.tempMax,
        tempMin = excluded.tempMin,
        tempAvg = excluded.tempAvg,
        precipitation = excluded.precipitation,
        windAvg = excluded.windAvg,
        windMax = excluded.windMax,
        visibilityMin = excluded.visibilityMin,
        visibilityAvg = excluded.visibilityAvg,
        snowAvg = excluded.snowAvg,
        atmPressure = excluded.atmPressure,
        dewPoint = excluded.dewPoint,
        humidity = excluded.humidity,
        sunDuration = excluded.sunDuration,
        phenomena = excluded.phenomena;