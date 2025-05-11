FROM openjdk:21-jdk-slim
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8080
CMD ["java", "-Dserver.address=0.0.0.0", "-Dserver.port=8080", "-jar", "app.jar"]

