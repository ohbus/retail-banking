# Build stage
FROM maven:3.8.4-openjdk-11 AS build

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn clean install

# Run stage
FROM openjdk:11-jre-slim

LABEL maintainer="Subhrodip Mohanta hello@subho.xyz"
LABEL artifact="retail-banking"
LABEL name="Retail Banking"

WORKDIR /app

COPY --from=build /app/target/retail.banking-1.0.jar .

EXPOSE 9998

ENTRYPOINT [ "java", "-jar", "retail.banking-1.0.jar" ]