# ── Stage 1: Maven 빌드 ──
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn package -DskipTests

# ── Stage 2: 실행 이미지 ──
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /app/target/auto-ledger-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", \
  "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:default}", \
  "-jar", "app.jar"]
