package com.jk.blog.utils;


import com.jk.blog.exception.DirectoryCreationException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

public class FileUtils {

    private FileUtils() {
        // Prevent instantiation
    }

    public static boolean isValidImage(String filename) {
        String lowerCaseFilename = filename.toLowerCase();
        return lowerCaseFilename.endsWith(".png") ||
                lowerCaseFilename.endsWith(".jpg") ||
                lowerCaseFilename.endsWith(".jpeg") ||
                lowerCaseFilename.endsWith(".gif");
    }

    public static boolean isValidVideo(String filename) {
        String lowerCaseFilename = filename.toLowerCase();
        return lowerCaseFilename.endsWith(".mp4") ||
                lowerCaseFilename.endsWith(".avi") ||
                lowerCaseFilename.endsWith(".mkv") ||
                lowerCaseFilename.endsWith(".mov");
    }

    public static String generateRandomFileName(String originalFilename) {
        String extension = originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : "";
        return UUID.randomUUID().toString() + extension;
    }

    public static Path createFilePath(String directory, String fileName) {
        try {
            Path path = Paths.get(directory).normalize();
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }
            return path.resolve(fileName);
        } catch (IOException e) {
            throw new DirectoryCreationException("Failed to create directory at " + directory);
        }
    }

}
