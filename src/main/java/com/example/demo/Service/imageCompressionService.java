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

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Iterator;

@Service
public class imageCompressionService {

    public String image(String imageUrl,String Sucode){
        try {
            // Create folder structure: baseOutput/code/downloaded-image
            Path codeFolder = Paths.get("test", Sucode);
            Path tmpFolder = Paths.get("tmp");

            Files.createDirectories(codeFolder);
            Files.createDirectories(tmpFolder);

            // Get file name from URL
            String fileName = imageUrl.substring(imageUrl.lastIndexOf('/') + 1);
            Path tmpFile = tmpFolder.resolve(fileName);
            Path outputPath = codeFolder.resolve(fileName);

            System.out.println("Downloading "+fileName+" "+Sucode);
            if (downloadImage(imageUrl, tmpFile.toFile())) {
                // Compress the image if download is successful
                System.out.println("Compressing "+fileName);
                compressImage(tmpFile.toFile(), outputPath.toFile(), 0.5f, 0.05f); // 50% quality
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

    public void compressImage(File inputFile, File outputFile, float quality, float ratio) {
        try {
            // Read the image
            BufferedImage originalImage = ImageIO.read(inputFile);
            if (originalImage == null) {
                System.out.println("Invalid image file: " + inputFile.getAbsolutePath());
                return;
            }

            System.out.println("Compressing image: " + inputFile.getAbsolutePath());

            // Resize the image based on the ratio
            int newWidth = (int) (originalImage.getWidth() * ratio);
            int newHeight = (int) (originalImage.getHeight() * ratio);
            BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = resizedImage.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
            g.dispose();

            // Get a JPEG ImageWriter
            Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
            if (!writers.hasNext()) {
                System.out.println("No JPEG writer available");
                return;
            }
            ImageWriter writer = writers.next();
            ImageWriteParam param = writer.getDefaultWriteParam();
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(quality); // Compression level (0.0 = max compression, 1.0 = max quality)

            // Write compressed image
            try (OutputStream os = new FileOutputStream(outputFile);
                 ImageOutputStream ios = ImageIO.createImageOutputStream(os)) {
                writer.setOutput(ios);
                writer.write(null, new IIOImage(resizedImage, null, null), param);
                writer.dispose();
                System.out.println("Compressed image saved: " + outputFile.getAbsolutePath());
            }
        } catch (Exception e) {
            System.err.println("Error compressing image: " + inputFile.getAbsolutePath());
            e.printStackTrace();
        }
    }


    public  void processExcel(String excelFilePath) {
        try (FileInputStream fis = new FileInputStream(excelFilePath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {

                Cell codeCell = row.getCell(0);
                Cell imagePathCell = row.getCell(1);

                if (codeCell != null && imagePathCell != null) {
                    String code = codeCell.getStringCellValue();
                    String imagePath = imagePathCell.getStringCellValue();

                    image(imagePath,code);

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
