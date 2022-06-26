FROM openjdk:17

ARG JAR_FILE=./target/yandex-market-0.0.1-SNAPSHOT.jar

WORKDIR /app

COPY ${JAR_FILE} app.jar

ENV PATH=./src/main/resources/static

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]

# Первый набросок, надо будет добавить бд и все дела