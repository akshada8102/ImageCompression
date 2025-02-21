package com.example.demo.Controller;

import com.example.demo.Service.imageCompressionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.io.*;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/v1/imagecompression")

public class imageCompressionController {
    @Autowired
    imageCompressionService _imagecompresssionservie;

    @GetMapping
    public String imagecompression(){
        String excelFilePath = Paths.get(".", "src", "main", "resources", "test.xlsx").toAbsolutePath().toString();

        _imagecompresssionservie.processExcel(excelFilePath);
     return "SUCCESS";
    }

}
