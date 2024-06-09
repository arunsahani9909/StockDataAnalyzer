FROM openjdk:17-jdk-alpine
WORKDIR /app
RUN mkdir -p src/main/resources/output src/main/resources/FinalOutput
COPY target/StockDataAnalyzer-0.0.1-SNAPSHOT.jar app.jar
COPY src/main/resources /app/src/main/resources
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]