FROM amazoncorretto:17-alpine

COPY ./web /web
COPY ./target/scala-2.13/WeatherTool-assembly-0.1.1-SNAPSHOT.jar /app.jar

VOLUME /data

CMD ["java", "-jar", "/app.jar"]