package com.example.StockDataAnalyzer.service;

import com.example.StockDataAnalyzer.model.StockData;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
public class StockService {

    /**This is pick up the file in resourses/input folder and will generate resources/output folder and will have
     * similar directories/subdirectories as input folder to understand the data
     * for each exchange
     * */
    public void processFiles(String inputFolderPath, String outputFolderPath) throws IOException {
        Files.createDirectories(Paths.get(outputFolderPath));

        Files.walk(Paths.get(inputFolderPath))
                .filter(Files::isRegularFile)
                .forEach(filePath -> {
                    try {
                        List<StockData> sampledData = getRandom30DataPoints(filePath.toString());
                        Path relativePath = Paths.get(inputFolderPath).relativize(filePath);
                        String outputFilePath = Paths.get(outputFolderPath, relativePath.toString()).toString();
                        Files.createDirectories(Paths.get(outputFilePath).getParent());
                        writeCSV(outputFilePath, sampledData);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
    }

    public List<StockData> getRandom30DataPoints(String filePath) throws IOException {
        List<StockData> data = readCSV(filePath);
        return getRandom30ConsecutiveDataPoints(data);
    }

    public void findOutliersInFiles(String outputFolderPath, String finalOutputFolderPath) throws IOException {
        Files.createDirectories(Paths.get(finalOutputFolderPath));

        Files.walk(Paths.get(outputFolderPath))
                .filter(Files::isRegularFile)
                .forEach(filePath -> {
                    try {
                        List<StockData> data = readCSV(filePath.toString());
                        List<StockData> outliers = findOutliers(data);
                        Path relativePath = Paths.get(outputFolderPath).relativize(filePath);
                        String finalOutputFilePath = Paths.get(finalOutputFolderPath, relativePath.toString()).toString();
                        Files.createDirectories(Paths.get(finalOutputFilePath).getParent());
                        writeOutliersCSV(finalOutputFilePath, outliers, data);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
    }



    /**
     * Reads a CSV file and returns a list of StockData objects
     * */
    public static List<StockData> readCSV(String filePath) throws IOException {
        List<StockData> records = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            Iterable<CSVRecord> csvRecords = CSVFormat.DEFAULT
                    .withHeader("Stock-ID", "Timestamp", "Stock Price")
                    .withSkipHeaderRecord()
                    .parse(br);

            // Convert each CSV record to a StockData object
            for (CSVRecord csvRecord : csvRecords) {
                StockData data = new StockData();
                data.setStockId(csvRecord.get("Stock-ID"));
                data.setTimestamp(csvRecord.get("Timestamp"));
                data.setStockPrice(Double.parseDouble(csvRecord.get("Stock Price")));
                records.add(data);
            }
        }
        return records;
    }

    public static List<StockData> getRandom30ConsecutiveDataPoints(List<StockData> data) {
        // Calculate the maximum starting index to ensure 30 consecutive points
        int maxStartIndex = data.size() - 30;
        // Generate a random starting index

        int startIndex = (int) (Math.random() * maxStartIndex);
        // Return 30 consecutive data points starting from the random index
        return new ArrayList<>(data.subList(startIndex, startIndex + 30));
    }

    /**
     * Finds and returns the outliers in the provided list of data points.
     * An outlier is any data point that is more than 2 standard deviations away from the mean.
     **/
    public static List<StockData> findOutliers(List<StockData> data) {
        // Calculate the mean
        double mean = data.stream().mapToDouble(StockData::getStockPrice).average().orElse(0.0);
        // Calculate the standard deviation
        double standardDeviation = Math.sqrt(data.stream()
                .mapToDouble(d -> Math.pow(d.getStockPrice() - mean, 2))
                .average()
                .orElse(0.0));
        double threshold = 2 * standardDeviation;

        // Find the outliers
        List<StockData> outliers = new ArrayList<>();
        for (StockData d : data) {
            double deviation = Math.abs(d.getStockPrice() - mean);
            if (deviation > threshold) {
                outliers.add(d);
            }
        }
        return outliers;
    }

    /**
     * Writes a list of StockData objects to a CSV file.
     * */
    public static void writeCSV(String filePath, List<StockData> data) throws IOException {
        try (FileWriter writer = new FileWriter(filePath);
             CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT
                     .withHeader("Stock-ID", "Timestamp", "Stock Price"))) {
            for (StockData record : data) {
                csvPrinter.printRecord(record.getStockId(), record.getTimestamp(), record.getStockPrice());
            }
        }
    }

    /**
     * Writes a list of outliers to a CSV file with additional columns for mean, deviation, and percentage deviation.
     **/
    public static void writeOutliersCSV(String filePath, List<StockData> outliers, List<StockData> data) throws IOException {
        // Calculate the mean
        double mean = data.stream().mapToDouble(StockData::getStockPrice).average().orElse(0.0);
        // Calculate the standard deviation
        double standardDeviation = Math.sqrt(data.stream()
                .mapToDouble(d -> Math.pow(d.getStockPrice() - mean, 2))
                .average()
                .orElse(0.0));
        double threshold = 2 * standardDeviation;

        try (FileWriter writer = new FileWriter(filePath);
             CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT
                     .withHeader("Stock-ID", "Timestamp", "Stock Price", "Mean", "Deviation", "% Deviation Over Threshold"))) {
            for (StockData outlier : outliers) {
                double deviation = outlier.getStockPrice() - mean;
                double percentDeviation = (Math.abs(deviation) - threshold) / threshold * 100;
                csvPrinter.printRecord(outlier.getStockId(), outlier.getTimestamp(), outlier.getStockPrice(), mean, deviation, percentDeviation);
            }
        }
    }

}
