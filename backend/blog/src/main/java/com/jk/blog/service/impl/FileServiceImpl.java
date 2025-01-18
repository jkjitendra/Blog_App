package com.jk.blog.service.impl;

import com.jk.blog.exception.DirectoryCreationException;
import com.jk.blog.exception.InvalidFileException;
import com.jk.blog.service.FileService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

@Service("localFileService")
public class FileServiceImpl implements FileService {

    @Override
    public String uploadImage(String path, MultipartFile file) throws IOException {
        if (file.isEmpty() || file.getOriginalFilename().isBlank()) {
            throw new InvalidFileException("Invalid file or filename.");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : "";
        String randomFileName = UUID.randomUUID().toString() + extension;
        String filePath = path + File.separator + randomFileName;

        File directory = new File(path);
        if (!directory.exists() && !directory.mkdirs()) {
            throw new DirectoryCreationException("Failed to create directory at " + path);
        }

        Files.copy(file.getInputStream(), Paths.get(filePath));
        return randomFileName;
    }

    @Override
    public String uploadVideo(String path, MultipartFile file) throws IOException {
        if (file.isEmpty() || file.getOriginalFilename().isBlank()) {
            throw new InvalidFileException("Invalid file or filename.");
        }

        String extension = file.getOriginalFilename().contains(".")
                ? file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."))
                : "";
        String randomFileName = UUID.randomUUID().toString() + extension;
        String filePath = path + File.separator + randomFileName;

        File directory = new File(path);
        if (!directory.exists() && !directory.mkdirs()) {
            throw new DirectoryCreationException("Failed to create directory at " + path);
        }

        Files.copy(file.getInputStream(), Paths.get(filePath));
        return randomFileName;
    }

    @Override
    public InputStream getResource(String path, String fileName) throws FileNotFoundException {
        String fullFilePath = path + File.separator + fileName;
        return new FileInputStream(fullFilePath);
    }
}
