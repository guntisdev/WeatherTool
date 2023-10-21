CREATE TABLE IF NOT EXISTS weather (
    dateTime TIMESTAMPTZ NOT NULL,
    city VARCHAR(255) NOT NULL,
    tempMax DOUBLE PRECISION,
    tempMin DOUBLE PRECISION,
    tempAvg DOUBLE PRECISION,
    precipitation DOUBLE PRECISION,
    windAvg DOUBLE PRECISION,
    windMax DOUBLE PRECISION,
    visibilityMin DOUBLE PRECISION,
    visibilityAvg DOUBLE PRECISION,
    snowAvg DOUBLE PRECISION,
    atmPressure DOUBLE PRECISION,
    dewPoint DOUBLE PRECISION,
    humidity DOUBLE PRECISION,
    sunDuration DOUBLE PRECISION,
    phenomena TEXT[],
    UNIQUE(city, dateTime)
)