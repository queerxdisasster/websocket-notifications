# ==== STAGE 1: Build ====
FROM maven:3.8.7-eclipse-temurin-11 AS build
WORKDIR /app

# Копируем settings.xml (с репо и логином/паролем)
COPY settings.xml /app/settings.xml

# Копируем pom.xml и скачиваем зависимости
COPY pom.xml .
RUN mvn dependency:go-offline -s /app/settings.xml

# Копируем исходный код и собираем приложение
COPY src ./src
RUN mvn package -DskipTests -s /app/settings.xml

# ==== STAGE 2: Runtime ====
FROM eclipse-temurin:11-jre

# Установка рабочего каталога
WORKDIR /app

# Копирование собранного JAR-файла из стадии сборки
COPY --from=build /app/target/websocketNotifications-*.jar /app/app.jar

# Установка переменных окружения для JVM
ENV JAVA_OPTS="-Xms512m -Xmx2G -XX:MaxDirectMemorySize=1G -Djava.net.preferIPv4Stack=true -DIGNITE_PERFORMANCE_SUGGESTIONS_DISABLED=true -DIGNITE_NO_SHUTDOWN_HOOK=true"

# Открытие необходимых портов
# 8080 - порт вашего Spring Boot приложения
EXPOSE 8081

# Команда запуска приложения с заданными JVM параметрами
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
