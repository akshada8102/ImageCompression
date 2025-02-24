package com.example.Image.Compression.Controller;

import com.example.Image.Compression.Service.ImageCompressionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/imagecompression")
public class ImageCompressionController {
    @Autowired
    ImageCompressionService _imageCompresssionServie;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadExcelFile(@RequestParam("file") MultipartFile excelFile) {
       _imageCompresssionServie.processExcel(excelFile);
        return ResponseEntity.ok("File compressed successfully");
    }

}
