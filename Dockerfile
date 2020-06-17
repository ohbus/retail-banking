FROM openjdk:11-jre-slim

LABEL maintainer="Subhrodip Mohanta"
LABEL email="hello@subho.xyz"
LABEL application="Retail Banking"

COPY target/retail.banking-0.1.0.jar /usr/local/retail.banking/

EXPOSE 8080

CMD ["java", "-jar", "/usr/local/retail.banking/retail.banking-0.1.0.jar"]
