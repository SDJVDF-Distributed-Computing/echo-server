FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn package -q --no-transfer-progress

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/smp-server.jar ./smp-server.jar
COPY entrypoint.sh .
RUN chmod +x entrypoint.sh
EXPOSE 8443
ENTRYPOINT ["./entrypoint.sh"]
