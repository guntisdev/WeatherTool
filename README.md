# Weather Tool

Tool for downloading historical and real-time data from 33 weather stations in Latvia. It aggregates and displays data for different time periods, cities, and weather parameters.

## Run
```
docker-compose up node
docker-compose up scala
```

## Run
```
// scala
sbt run

// web
cd web/
npm run dev
```

**Author:** Guntis Smaukstelis  
**Mentor:** Jānis Komuls

**Status:** Live  
**URL:** [https://weather-tool.fly.dev/](https://weather-tool.fly.dev/)  
**Used By:** State television weather news  
**Repository:** [https://github.com/guntisdev/WeatherTool](https://github.com/guntisdev/WeatherTool)

## Backend Technologies:
- cats-effects
- http4s
- pureconfig
- fs2
- circe
- scalatest

## Frontend Technologies:
- SolidJS
- Vite

## Deployment:
- Docker
- Shell script
- Fly.io

## Database
```
SELECT * FROM public.weather WHERE datetime > '2023-12-9T00:00:00+00'::timestamptz;
DELETE FROM public.weather
SET TIMEZONE = 'Europe/Riga';
```

## Docker
```
docker pull postgres
docker run --name local-postgres -e POSTGRES_PASSWORD=mysecretpassword -p 5432:5432 -d postgres
docker stop local-postgres
docker start local-postgres
```

## Fly Postgres
```
fly machines list --app weather-tool-db
fly machines start <machine-id> --app weather-tool-db

fly proxy 15432:5432 -a weather-tool-db // map to local port for pgAdmin
```