# Weather Tool

Tool for downloading historical and real-time data from 33 weather stations in Latvia. It aggregates and displays data for different time periods, cities, and weather parameters.

## Setup
Rename `.env-sample` to `.env` and fill in credentials

## Run
```
docker-compose up node
docker-compose up scala
```

## Web
[http://0.0.0.0:9080/](http://0.0.0.0:9080/)

## Local development
```
// 1st terminal
sbt

// 2nd terminal
cd web/
npm run dev
```

## Backend Tech:
- scala: cats-effects, http4s, fs2, circe, scalatest
- postgres

## Frontend Tech:
- SolidJS
- Vite