# Build stage
FROM maven:3-amazoncorretto-21-debian AS build

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn clean package -DskipTests

# Run stage
FROM amazoncorretto:21-alpine

LABEL maintainer="Subhrodip Mohanta hello@subho.xyz"
LABEL artifact="retail-banking"
LABEL name="Retail Banking"
LABEL org.opencontainers.image.source="https://github.com/ohbus/retail-banking"

WORKDIR /app

COPY --from=build /app/target/retail.banking-1.0.jar .

EXPOSE 9998

ENV SPRING_PROFILES_ACTIVE=prod

ENTRYPOINT [ "java", "-jar", "retail.banking-1.0.jar" ]
