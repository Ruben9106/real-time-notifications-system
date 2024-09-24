FROM openjdk:17-jdk-slim
VOLUME /tmp
COPY target/notification-service.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]