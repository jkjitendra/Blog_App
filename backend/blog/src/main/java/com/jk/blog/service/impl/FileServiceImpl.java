package com.jk.blog.service.impl;

import com.jk.blog.exception.InvalidFileException;
import com.jk.blog.exception.InvalidFormatException;
import com.jk.blog.service.FileService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.jk.blog.utils.FileUtils.*;

@Service("localFileService")
public class FileServiceImpl implements FileService {


    @Override
    public String uploadImage(String path, MultipartFile file) throws IOException {

        validateFile(file, true);
        return saveFile(path, file);
    }

    @Override
    public String uploadVideo(String path, MultipartFile file) throws IOException {

        validateFile(file, false);
        return saveFile(path, file);
    }

    private String saveFile(String path, MultipartFile file) throws IOException {
        String randomFileName = generateRandomFileName(file.getOriginalFilename());
        Path filePath = createFilePath(path, randomFileName);
        Files.copy(file.getInputStream(), filePath);
        return randomFileName;
    }

    @Override
    public InputStream getResource(String path, String fileName) throws FileNotFoundException {
        Path fullPath = Paths.get(path, fileName).normalize();
        try {
            if (!Files.exists(fullPath)) {
                throw new FileNotFoundException("File not found at path: " + fullPath);
            }
            if (!Files.isReadable(fullPath)) {
                throw new FileNotFoundException("File at path is not readable: " + fullPath);
            }
            return Files.newInputStream(fullPath);
        } catch (IOException e) {
            throw new FileNotFoundException("Error opening file at path: " + fullPath + ". " + e.getMessage());
        }
    }

    private static void validateFile(MultipartFile file, boolean isImage) {

        if (file.isEmpty() || file.getOriginalFilename().isBlank()) {
            throw new InvalidFileException("Invalid file or filename.");
        }

        String filename = file.getOriginalFilename().toLowerCase();
        if (isImage && !isValidImage(filename)) {
            throw new InvalidFormatException("Unsupported image format. Allowed formats: .png, .jpg, .jpeg, .gif");
        }
        if (!isImage && !isValidVideo(filename)) {
            throw new InvalidFormatException("Unsupported video format. Allowed formats: .mp4, .avi, .mkv, .mov");
        }
    }
}
