FROM amazoncorretto:17-alpine

WORKDIR /app

COPY ./web/dist/ web/dist/
COPY ./target/scala-2.13/app.jar app.jar

VOLUME /app/data


# flags setting heap to 256mb and max 384mb, plus g1 garbage collector
# CMD ["java", "-Xms256m", "-Xmx384m", "-XX:+UseG1GC", "-jar", "app.jar"]
# CMD ["java", "-jar", "app.jar"]
CMD ["java", "-Xms512m", "-Xmx600m", "-XX:+UseG1GC", "-XX:MaxGCPauseMillis=200", "-jar", "app.jar"]