package com.jk.blog.utils;

import com.jk.blog.exception.DirectoryCreationException;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FileUtilsTest {

    @Test
    void test_IsValidImage_ShouldReturnTrue_WhenValidImageExtensions() {
        assertTrue(FileUtils.isValidImage("image.png"));
        assertTrue(FileUtils.isValidImage("photo.JPG"));
        assertTrue(FileUtils.isValidImage("picture.jpeg"));
        assertTrue(FileUtils.isValidImage("animation.gif"));
    }

    @Test
    void test_IsValidImage_ShouldReturnFalse_WhenInvalidExtensions() {
        assertFalse(FileUtils.isValidImage("document.pdf"));
        assertFalse(FileUtils.isValidImage("video.mp4"));
        assertFalse(FileUtils.isValidImage("music.mp3"));
        assertFalse(FileUtils.isValidImage("spreadsheet.xlsx"));
    }

    @Test
    void test_IsValidImage_ShouldReturnFalse_WhenNoExtension() {
        assertFalse(FileUtils.isValidImage("filename"));
    }

    @Test
    void test_IsValidVideo_ShouldReturnTrue_WhenValidVideoExtensions() {
        assertTrue(FileUtils.isValidVideo("movie.mp4"));
        assertTrue(FileUtils.isValidVideo("clip.AVI"));
        assertTrue(FileUtils.isValidVideo("series.mkv"));
        assertTrue(FileUtils.isValidVideo("trailer.MOV"));
    }

    @Test
    void test_IsValidVideo_ShouldReturnFalse_WhenInvalidExtensions() {
        assertFalse(FileUtils.isValidVideo("image.png"));
        assertFalse(FileUtils.isValidVideo("audio.mp3"));
        assertFalse(FileUtils.isValidVideo("document.docx"));
        assertFalse(FileUtils.isValidVideo("presentation.pptx"));
    }

    @Test
    void test_IsValidVideo_ShouldReturnFalse_WhenNoExtension() {
        assertFalse(FileUtils.isValidVideo("filename"));
    }

    @Test
    void test_GenerateRandomFileName_ShouldGenerateUniqueNames_WhenCalledMultipleTimes() {
        String originalFilename = "document.pdf";
        String generatedFileName1 = FileUtils.generateRandomFileName(originalFilename);
        String generatedFileName2 = FileUtils.generateRandomFileName(originalFilename);

        assertNotEquals(generatedFileName1, generatedFileName2);
        assertTrue(generatedFileName1.endsWith(".pdf"));
        assertTrue(generatedFileName2.endsWith(".pdf"));
    }

    @Test
    void test_GenerateRandomFileName_ShouldHandleFilesWithoutExtension() {
        String generatedFileName = FileUtils.generateRandomFileName("file");
        assertFalse(generatedFileName.contains("."));
    }

    @Test
    void test_CreateFilePath_ShouldReturnValidPath_WhenDirectoryExists() {
        String directory = "test_dir";
        String fileName = "test_file.txt";

        Path path = FileUtils.createFilePath(directory, fileName);
        assertNotNull(path);
        assertEquals(directory + "/" + fileName, path.toString());

        // Cleanup
        assertTrue(Files.exists(path.getParent()));
        path.toFile().delete();
        path.getParent().toFile().delete();
    }

    @Test
    void test_CreateFilePath_ShouldThrowException_WhenInvalidDirectory() {
        String invalidDirectory = "/invalid/|path";
        String fileName = "test.txt";

        DirectoryCreationException exception = assertThrows(
                DirectoryCreationException.class,
                () -> FileUtils.createFilePath(invalidDirectory, fileName)
        );

        assertEquals("Failed to create directory at " + invalidDirectory, exception.getMessage());
    }
}