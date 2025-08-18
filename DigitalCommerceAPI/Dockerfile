
FROM bellsoft/liberica-openjdk-alpine:21


WORKDIR /app


COPY target/DigitalEcommerceAPI-0.0.1-SNAPSHOT.jar digitalcommerce.jar


ENTRYPOINT ["java", "-jar", "digitalcommerce.jar"]


EXPOSE 8080
