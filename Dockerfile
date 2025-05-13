FROM openjdk:21-jdk-slim
WORKDIR /app
COPY target/Telegram-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
CMD ["java", "-Dserver.address=0.0.0.0", "-Dserver.port=8080", "-jar", "app.jar"]
