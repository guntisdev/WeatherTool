FROM amazoncorretto:17-alpine

COPY ./web/dist/ /web/dist/
COPY ./target/scala-2.13/WeatherTool-assembly-0.1.1-SNAPSHOT.jar /app.jar

VOLUME /app/data

CMD ["java", "-jar", "/app.jar"]