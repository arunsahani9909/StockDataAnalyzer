# For setting up poject below needed
- Java 17
- Maven
- Docker

# To build and run application

1. Clone the repository
    bash
    git clone urll
    cd StockDataAnalyzer

2. Build the application
    mvn clean install -DskipTests=true


3. Build the Docker image
   docker build -t stock-data-analyzer .

4. Run the Docker container
   docker run -p 8080:8080 stock-data-analyzer

# API Endpoints
- `/api/stocks/data` (POST): Upload a CSV file to get 30 consecutive data points.
- `/api/stocks/outliers` (POST): Get outliers from the provided data points.

#Below are the curl requests which can be imported in postman
1. POST: curl --location --request POST 'localhost:8080/api/stocks/data'
This is will give the response as "Output is generated in  the path src/main/resources/output/"
2. GET: curl --location 'http://localhost:8080/api/stocks/outliers'
This is will give the response as "Output is generated in  the path src/main/resources/FinalOutPut/"


# Exception
- Exception handling for missing file, invalid CSV format, and empty file.

 
 