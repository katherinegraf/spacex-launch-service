FROM postgres:15.0
ENV POSTGRES_USER postgres
ENV POSTGRES_PASSWORD postgres

FROM gradle:7.4.1 AS build
COPY --chown=gradle:gradle . /builder
WORKDIR /builder
RUN gradle build -x test

FROM eclipse-temurin:17-jdk
WORKDIR /app
EXPOSE 8080
COPY --from=build /builder/build/libs/spacex-0.0.1-SNAPSHOT.jar /app/app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
