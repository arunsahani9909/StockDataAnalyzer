package com.example.StockDataAnalyzer.Contoller;

import com.example.StockDataAnalyzer.model.StockData;
import com.example.StockDataAnalyzer.service.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/stocks")
public class StockController {


    @Autowired
    private StockService stockService;

    @GetMapping("/test")
    public String test()  {
        return "Hello! Application is up and running";

    }

    /**
     * Endpoint to process all files in the input folder and its subdirectories
     * random timestamp, extract 30 consecutive data points within each file
     * and store the response in the output folder
     */
    @PostMapping("/data")
    public String processFiles() throws IOException {
        String inputFolderPath = "src/main/resources/input/";
        String outputFolderPath = "src/main/resources/output/";

        stockService.processFiles(inputFolderPath, outputFolderPath);
        return "Output is generated in  the path src/main/resources/output/";
    }

    /**
     * Endpoint to get the list of outliers from the processed data from /data api in the output folder
     * and store the result in the FinalOutput folder.
     */
    @GetMapping("/outliers")
    public String getOutliers() throws IOException {
        String outputFolderPath = "src/main/resources/output/";
        String finalOutputFolderPath = "src/main/resources/FinalOutput/";
        stockService.findOutliersInFiles(outputFolderPath, finalOutputFolderPath);
        return "Output is generated in  the path src/main/resources/FinalOutPut/";
    }
}
