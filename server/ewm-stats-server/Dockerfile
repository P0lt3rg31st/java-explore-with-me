# ---------- build stage ----------
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace

# Копируем весь репозиторий (потому что multi-module, нужен root pom и модули)
COPY . .

# Собираем только stats-server и всё, что ему нужно (-am подтянет зависимости-модули)
RUN mvn -pl server/ewm-stats-server -am -DskipTests package

# ---------- run stage ----------
FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /workspace/server/ewm-stats-server/target/ewm-stats-server-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 9090
ENTRYPOINT ["java","-jar","/app/app.jar"]
