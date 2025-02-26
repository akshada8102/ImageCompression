package com.example.Image.Compression.Service;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class ImageCompressionService {

    private final ExecutorService executor = Executors.newFixedThreadPool(10);

    public void image(String imageUrl, String sucode, int count) {
        System.out.println("Image :: " + count);
        try {
            // Creating new folder
            Path codeFolder = Paths.get("Anthology_TopView_CompressedImages", sucode);
            Path tmpFolder = Paths.get("Anthology_TopView_OriginalImages", sucode);

            Files.createDirectories(codeFolder);
            Files.createDirectories(tmpFolder);

            // Get file name from URL
            String fileName = imageUrl.substring(imageUrl.lastIndexOf('/') + 1);
            Path tmpFile = tmpFolder.resolve(fileName);
            Path outputPath = codeFolder.resolve(fileName);

            System.out.println("Downloading Image " + count + " :: " + fileName + " " + sucode);
            if (downloadImage(imageUrl, tmpFile.toFile())) {
                // Compress the image if download is successful
                System.out.println("Compressing Image :: " + fileName);
                compressImage(tmpFile.toFile(), outputPath.toFile(), 0.5f, 0.5f); // 50% quality
            }

        } catch (Exception ex) {
            System.out.println("Error processing image: " + ex.getMessage());
            ex.printStackTrace();
        }

    }

    public static boolean downloadImage(String imageUrl, File destinationFile) {
        HttpURLConnection httpConn=null;
        int retries = 3;
        while (retries > 0) {
            try {
                URL url = new URL(imageUrl);
                httpConn = (HttpURLConnection) url.openConnection();
                httpConn.setRequestProperty("User-Agent", "Mozilla/5.0");
                int responseCode = httpConn.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    try (InputStream inputStream = httpConn.getInputStream();
                         FileOutputStream outputStream = new FileOutputStream(destinationFile)) {

                        byte[] buffer = new byte[8192];
                        int bytesRead;
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                        }
                        System.out.println("Downloaded image :: " + destinationFile.getAbsolutePath());
                        return true;
                    } catch (Exception ex) {
                        System.err.println("Error downloading image :: " + ex.getMessage());
                    }
                } else {
                    System.out.println("Failed to download image :: " + imageUrl + " (HTTP " + responseCode + ")");
                }
                httpConn.disconnect();
            } catch (Exception e) {
                System.err.println("Error downloading image from :: " + imageUrl);
                e.printStackTrace();
            } finally {
                if (httpConn != null) {
                    httpConn.disconnect();
                }
            }
            retries--;
            if (retries > 0) {
                System.out.println("Retrying download... (" + retries + " retries left)");
            }
        }
        return false;
    }

    public void compressImage(File inputFile, File outputFile, float quality, float ratio) {
        try {
            // Read the image
            BufferedImage originalImage = ImageIO.read(inputFile);
            if (originalImage == null) {
                System.out.println("Invalid image file :: " + inputFile.getAbsolutePath());
                return;
            }

            System.out.println("Compressing Image :: " + inputFile.getAbsolutePath());

            // Resize the image based on the ratio
            int newWidth = (int) (originalImage.getWidth() * ratio);
            int newHeight = (int) (originalImage.getHeight() * ratio);
            BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = resizedImage.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
            g.dispose();

            originalImage.flush();
            originalImage=null;

            File renamedOutputFile = new File(outputFile.getParent(), "Anthology_TopView_Compressed.jpg");
            ImageIO.write(resizedImage, "jpg", renamedOutputFile);
            System.out.println("Compressed Image Saved: " + outputFile.getAbsolutePath());

            resizedImage.flush();
            resizedImage=null;

            System.gc();
        } catch (Exception e) {
            System.err.println("Error compressing Image :: " + inputFile.getAbsolutePath());
            e.printStackTrace();
        }
    }

    public void processExcel(MultipartFile excelFile) {
        int count = 0;
        try (Workbook workbook = new XSSFWorkbook(excelFile.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            boolean firstRow = true;
            for (Row row : sheet) {
                if (firstRow) {
                    firstRow = false; // Skip the first row
                    continue;
                }
                Cell codeCell = row.getCell(0);
                Cell imagePathCell = row.getCell(1);
                if (codeCell != null && imagePathCell != null) {
                    String code = codeCell.getStringCellValue();
                    String imagePath = imagePathCell.getStringCellValue();
                    System.out.println(code);
                    System.out.println(imagePath);
                    count++;
                    image(imagePath, code, count);
                    if(count%100==0){
                        System.gc();
//                      //  Thread.sleep(500);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
