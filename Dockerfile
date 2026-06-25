FROM amazoncorretto:25

WORKDIR /app

COPY target/zero-waste-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT [ "java", "-jar","app.jar" ]
