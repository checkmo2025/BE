FROM gradle:8.14.2-jdk21 AS builder

COPY . /usr/src
WORKDIR /usr/src
RUN chmod +x ./gradlew
RUN ./gradlew clean build -x test

FROM openjdk:21-jdk-slim

WORKDIR /app
COPY --from=builder /usr/src/build/libs/checkmo-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]