package com.jk.blog.service.impl;

import com.jk.blog.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Optional;
import java.util.UUID;

@Service("s3FileService")
public class S3FileServiceImpl implements FileService {

    @Autowired
    private S3Client s3Client;

    @Value("${aws.s3.bucketName}")
    private String bucketName;

    @Value("${aws.s3.region}")
    private String region;

    public String uploadImage(String path, MultipartFile file) throws IOException {
        String fileName = generateFileName(file); // Transform the filename first
        String key = path + fileName;
        return uploadFile(key, file);
    }

    public String uploadVideo(String path, MultipartFile file) throws IOException {
        String fileName = generateFileName(file);
        String key = path + fileName;
        return uploadFile(key, file);
    }

    private String uploadFile(String keyPrefix, MultipartFile file) throws IOException {
        String fileName = generateFileName(file);
        String key = keyPrefix + fileName;

        try (InputStream inputStream = file.getInputStream()) {

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(inputStream, file.getSize()));

            return "https://" + bucketName + ".s3." + region + ".amazonaws.com/" + key;
        } catch (S3Exception e) {
            String errorMessage = e.awsErrorDetails() != null ? e.awsErrorDetails().errorMessage() : e.getMessage();
            throw new IOException("Error uploading file to S3: " + errorMessage, e);
        }
    }

    private String generateFileName(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename.isEmpty()) {
            originalFilename = "file";
        }
        // Extract the extension
        String extension = "";
        int dotIndex = originalFilename.lastIndexOf(".");
        if (dotIndex >= 0) {
            extension = originalFilename.substring(dotIndex);
            originalFilename = originalFilename.substring(0, dotIndex); // Remove extension
        }

        // Replace spaces with underscores
        String safeFilename = originalFilename.replace(" ", "_");

        // Return UUID + original filename + extension
        return UUID.randomUUID().toString() + "-" + safeFilename + extension;
    }

    @Override
    public InputStream getResource(String path, String fileName) throws IOException {
        try {
            String s3Url = "https://" + bucketName + ".s3." + region + ".amazonaws.com/" + path + "/" + fileName;
            return new URL(s3Url).openStream();
        } catch (Exception e) {
            throw new IOException("Error fetching file from S3: " + e.getMessage(), e);
        }
    }

}
