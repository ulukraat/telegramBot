# Используем официальный образ JDK 21
FROM eclipse-temurin:21-jdk-alpine

# Указываем рабочую директорию
WORKDIR /app

# Копируем JAR-файл в контейнер
COPY target/Telegram-0.0.1-SNAPSHOT.jar /app/Telegram-0.0.1-SNAPSHOT.jar

# Устанавливаем команду запуска
CMD ["java", "-jar", "/app/Telegram-0.0.1-SNAPSHOT.jar"]
