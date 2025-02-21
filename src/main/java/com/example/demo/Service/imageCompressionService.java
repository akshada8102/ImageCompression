package com.example.demo.Service;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class imageCompressionService {

    public String image(String imageUrl,String Sucode){
        try {
            System.out.println("Inider");
            // Create folder structure: baseOutput/code/downloaded-image
            Path codeFolder = Paths.get("test", Sucode);

            // Get file name from URL
            String fileName = imageUrl.substring(imageUrl.lastIndexOf('/') + 1);
            Path outputPath = codeFolder.resolve(fileName);

            if (downloadImage(imageUrl, outputPath.toFile())) {
                // Compress the image if download is successful
                compressImage(outputPath.toFile(), outputPath.toFile(), 0.5f); // 50% quality
            }

        }catch(Exception ex){

        }
                return imageUrl;
    }


    public static boolean downloadImage(String imageUrl, File destinationFile) {
        try {
            URL url = new URL(imageUrl);
            HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
            httpConn.setRequestProperty("User-Agent", "Mozilla/5.0");
            int responseCode = httpConn.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (InputStream inputStream = httpConn.getInputStream();
                     FileOutputStream outputStream = new FileOutputStream(destinationFile)) {

                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                    System.out.println("Downloaded image: " + destinationFile.getAbsolutePath());
                    return true;
                }
            } else {
                System.out.println("Failed to download image: " + imageUrl + " (HTTP " + responseCode + ")");
            }
            httpConn.disconnect();
        } catch (Exception e) {
            System.err.println("Error downloading image from " + imageUrl);
            e.printStackTrace();
        }
        return false;
    }

    public static void compressImage(File inputFile, File outputFile, float quality) {
        try {
            // Read the image
            BufferedImage image = ImageIO.read(inputFile);
            if (image == null) {
                System.out.println("Invalid image file: " + inputFile.getAbsolutePath());
                return;
            }

            System.out.println("Compressing image: " + inputFile.getAbsolutePath());

            // Get a JPEG ImageWriter
            ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
            ImageWriteParam param = writer.getDefaultWriteParam();
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(quality); // Compression level (0.0 = max compression, 1.0 = max quality)

            // Write compressed image
            try (OutputStream os = new FileOutputStream(outputFile);
                 ImageOutputStream ios = ImageIO.createImageOutputStream(os)) {
                writer.setOutput(ios);
                writer.write(null, new IIOImage(image, null, null), param);
                writer.dispose();
                System.out.println("Compressed image saved: " + outputFile.getAbsolutePath());
            }
        } catch (Exception e) {
            System.err.println("Error compressing image: " + inputFile.getAbsolutePath());
            e.printStackTrace();
        }
    }


    public  void processExcel(String excelFilePath, String baseOutputPath) {
        try (FileInputStream fis = new FileInputStream(excelFilePath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // Skip header row

                Cell codeCell = row.getCell(0);
                Cell imagePathCell = row.getCell(1);

                if (codeCell != null && imagePathCell != null) {
                    String code = codeCell.getStringCellValue();
                    String imagePath = imagePathCell.getStringCellValue();

                    File sourceImage = new File(imagePath);
                    if (!sourceImage.exists()) {
                        System.out.println("Image not found: " + imagePath);
                        continue;
                    }

                    // Create folder structure
                    image(imagePath,code);

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
