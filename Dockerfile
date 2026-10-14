# ---- Stage 1: build ----
FROM eclipse-temurin:17-jdk-jammy AS build

WORKDIR /app

COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle
RUN chmod +x gradlew \
    && ./gradlew dependencies --no-daemon

COPY src ./src
RUN ./gradlew bootJar --no-daemon -x test

# ---- Stage 2: runtime ----
FROM eclipse-temurin:17-jre-jammy

RUN groupadd --system app && useradd --system --gid app --home /app app

WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

RUN chown -R app:app /app
USER app

EXPOSE 8081

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -XX:+UseContainerSupport"

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
