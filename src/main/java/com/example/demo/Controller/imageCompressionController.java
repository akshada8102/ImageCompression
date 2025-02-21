package com.example.demo.Controller;

import com.example.demo.Service.imageCompressionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/imagecompression")

public class imageCompressionController {
    @Autowired
    imageCompressionService _imagecompresssionservie;

    @GetMapping
    public String imagecompression(){
        String excelFilePath = "C://Users/sabal/Download/yourExcelFile.xlsx"; // Change to your actual Excel file path
        String baseOutputPath = "output"; // Base output directory

        _imagecompresssionservie.processExcel(excelFilePath, baseOutputPath);
        _imagecompresssionservie.image("https://unbxd-pim-assets.s3.us-east-1.amazonaws.com/images/cadedcbe922732ec1b1e0f0c510077af/1739298975/A2ZDAAAAAAAAAAAA10-Anthology_FrontView.jpg","M001");
     return "SUCCESS";
    }

}
