package com.jk.blog.service;

import com.jk.blog.exception.InvalidFileException;
import com.jk.blog.exception.InvalidFormatException;
import com.jk.blog.service.impl.FileServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FileServiceImplTest {

    private FileServiceImpl fileService;

    @TempDir
    Path tempDir;

    private MultipartFile mockMultipartFile;

    @BeforeEach
    void setUp() {
        fileService = new FileServiceImpl();
        mockMultipartFile = mock(MultipartFile.class);
    }

    @Test
    void testUploadImage_Success() throws IOException {
        when(mockMultipartFile.getOriginalFilename()).thenReturn("image.jpg");
        when(mockMultipartFile.isEmpty()).thenReturn(false);
        when(mockMultipartFile.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[10]));

        String fileName = fileService.uploadImage(tempDir.toString(), mockMultipartFile);

        assertNotNull(fileName);
        assertTrue(fileName.endsWith(".jpg"));
        assertTrue(Files.exists(tempDir.resolve(fileName)));
    }

    @Test
    void testUploadVideo_Success() throws IOException {
        when(mockMultipartFile.getOriginalFilename()).thenReturn("video.mp4");
        when(mockMultipartFile.isEmpty()).thenReturn(false);
        when(mockMultipartFile.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[10]));

        String fileName = fileService.uploadVideo(tempDir.toString(), mockMultipartFile);

        assertNotNull(fileName);
        assertTrue(fileName.endsWith(".mp4"));
        assertTrue(Files.exists(tempDir.resolve(fileName)));
    }

    @Test
    void testUploadImage_EmptyFile_ShouldThrowException() {
        when(mockMultipartFile.getOriginalFilename()).thenReturn("");
        when(mockMultipartFile.isEmpty()).thenReturn(true);

        Exception exception = assertThrows(InvalidFileException.class,
                () -> fileService.uploadImage(tempDir.toString(), mockMultipartFile));

        assertEquals("Invalid file or filename.", exception.getMessage());
    }

    @Test
    void testUploadVideo_EmptyFile_ShouldThrowException() {
        when(mockMultipartFile.getOriginalFilename()).thenReturn("");
        when(mockMultipartFile.isEmpty()).thenReturn(true);

        Exception exception = assertThrows(InvalidFileException.class,
                () -> fileService.uploadVideo(tempDir.toString(), mockMultipartFile));

        assertEquals("Invalid file or filename.", exception.getMessage());
    }

    @Test
    void testUploadImage_InvalidFormat_ShouldThrowException() {
        when(mockMultipartFile.getOriginalFilename()).thenReturn("image.txt");
        when(mockMultipartFile.isEmpty()).thenReturn(false);

        Exception exception = assertThrows(InvalidFormatException.class,
                () -> fileService.uploadImage(tempDir.toString(), mockMultipartFile));

        assertEquals("Unsupported image format. Allowed formats: .png, .jpg, .jpeg, .gif", exception.getMessage());
    }

    @Test
    void testUploadVideo_InvalidFormat_ShouldThrowException() {
        when(mockMultipartFile.getOriginalFilename()).thenReturn("video.doc");
        when(mockMultipartFile.isEmpty()).thenReturn(false);

        Exception exception = assertThrows(InvalidFormatException.class,
                () -> fileService.uploadVideo(tempDir.toString(), mockMultipartFile));

        assertEquals("Unsupported video format. Allowed formats: .mp4, .avi, .mkv, .mov", exception.getMessage());
    }

    @Test
    void testGetResource_FileExists_ShouldReturnInputStream() throws IOException {
        String testFileName = "test-file.txt";
        Path filePath = tempDir.resolve(testFileName);
        Files.write(filePath, "test content".getBytes());

        InputStream inputStream = fileService.getResource(tempDir.toString(), testFileName);

        assertNotNull(inputStream);
        assertEquals("test content", new String(inputStream.readAllBytes()));
    }

    @Test
    void testGetResource_FileNotFound_ShouldThrowException() {
        Exception exception = assertThrows(FileNotFoundException.class,
                () -> fileService.getResource(tempDir.toString(), "nonexistent.txt"));

        assertTrue(exception.getMessage().contains("File not found"));
    }

    @Test
    void testGetResource_FileNotReadable_ShouldThrowException() throws IOException {
        String testFileName = "unreadable.txt";
        Path filePath = tempDir.resolve(testFileName);
        Files.write(filePath, "test content".getBytes());
        filePath.toFile().setReadable(false); // Make the file unreadable

        Exception exception = assertThrows(FileNotFoundException.class,
                () -> fileService.getResource(tempDir.toString(), testFileName));

        assertTrue(exception.getMessage().contains("File at path is not readable"));
    }
}