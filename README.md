# Weather Tool

Tool for downloading historical and real-time data from 33 weather stations in Latvia. It aggregates and displays data for different time periods, cities, and weather parameters.

## Setup
Rename `.env-sample` to `.env` and fill in credentials

## Run
```
docker-compose up node --build
docker-compose up scala --build
```

## Web
[http://0.0.0.0:9090/](http://0.0.0.0:9090/)

## nginx proxy config
```
server {
    listen 80;
    server_name laikazinas.lsm.lv;

    location / {
        proxy_pass http://localhost:9090; # Forward requests to the Scala app
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host $host;
        proxy_cache_bypass $http_upgrade;
    }
}
```

## Local development
```
// 1st terminal
podman-compose up postgres

// 2nd terminal
sbt run

// 3rd terminal
cd web/
npm run dev
```

Run one file
```sbt "runMain grib.GribParser"```

## Backend Tech:
- scala: cats-effects, http4s, fs2, circe, scalatest
- postgres

## Frontend Tech:
- SolidJS
- Vite

## Fly commands
```
// suspend instance
fly scale count 0
```

```
fly scale memory 512
```