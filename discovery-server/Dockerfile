FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY product-service/pom.xml product-service/pom.xml
COPY order-service/pom.xml order-service/pom.xml
COPY inventory-service/pom.xml inventory-service/pom.xml
COPY discovery-server/pom.xml discovery-server/pom.xml
COPY api-gateway/pom.xml api-gateway/pom.xml
RUN mvn -B -pl discovery-server -am dependency:go-offline
COPY discovery-server/src ./discovery-server/src
RUN mvn -B -pl discovery-server -am clean package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/discovery-server/target/*-SNAPSHOT.jar app.jar
EXPOSE 8761
ENTRYPOINT ["java", "-jar", "app.jar"]
