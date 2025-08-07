FROM gradle:8.14.2-jdk21 AS builder

COPY . /usr/src
WORKDIR /usr/src
RUN chmod +x ./gradlew
RUN ./gradlew clean build -x test

FROM openjdk:21-jdk-slim

RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY --from=builder /usr/src/build/libs/*SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]