# ---------- build stage ----------
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

# POM'ы (для кеша зависимостей)
COPY pom.xml /app/pom.xml
COPY server/pom.xml /app/server/pom.xml
COPY server/ewm-main-server/pom.xml /app/server/ewm-main-server/pom.xml

COPY server/ewm-stats-server/pom.xml /app/server/ewm-stats-server/pom.xml

COPY dto/pom.xml /app/dto/pom.xml

COPY client/pom.xml /app/client/pom.xml
COPY client/ewm-stats-client/pom.xml /app/client/ewm-stats-client/pom.xml

# Тянем зависимости под нужный модуль
RUN mvn -q -pl server/ewm-main-server -am -DskipTests dependency:go-offline \
  -Dmaven.wagon.http.retryHandler.count=5 \
  -Dmaven.wagon.http.retryHandler.requestSentEnabled=true

# Исходники
COPY dto /app/dto
COPY client /app/client
COPY server /app/server

# Сборка main
RUN mvn -q -pl server/ewm-main-server -am -DskipTests package \
  -Dmaven.wagon.http.retryHandler.count=5 \
  -Dmaven.wagon.http.retryHandler.requestSentEnabled=true

# ---------- runtime stage ----------
FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /app/server/ewm-main-server/target/*SNAPSHOT.jar app.jar

ENV JAVA_OPTS=""
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]