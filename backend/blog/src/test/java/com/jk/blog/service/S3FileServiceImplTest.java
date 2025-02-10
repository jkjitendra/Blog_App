package com.jk.blog.service;

import com.jk.blog.service.impl.S3FileServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class S3FileServiceImplTest {

    @InjectMocks
    private S3FileServiceImpl s3FileService;

    @Mock
    private S3Client s3Client;

    @Mock
    private MultipartFile multipartFile;

    private static final String BUCKET_NAME = "test-bucket";
    private static final String REGION = "us-east-1";
    private static final String TEST_PATH = "test-folder/";
    private static final String TEST_FILE_NAME = "test-image.png";
    private static final String TEST_VIDEO_FILE_NAME = "test-video.mp4";
    private static final String EXPECTED_S3_URL = "https://" + BUCKET_NAME + ".s3." + REGION + ".amazonaws.com/" + TEST_PATH + TEST_FILE_NAME;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        ReflectionTestUtils.setField(s3FileService, "bucketName", BUCKET_NAME);
        ReflectionTestUtils.setField(s3FileService, "region", REGION);

    }

    @Test
    void testUploadImage_ShouldReturnS3Url_WhenUploadSuccessful() throws IOException {
        when(multipartFile.getOriginalFilename()).thenReturn(TEST_FILE_NAME);
        when(multipartFile.getContentType()).thenReturn("image/png");
        when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[10]));
        when(multipartFile.getSize()).thenReturn(10L);

        String result = s3FileService.uploadImage(TEST_PATH, multipartFile);

        assertNotNull(result);
        assertTrue(result.startsWith("https://test-bucket.s3.us-east-1.amazonaws.com/"));
        verify(s3Client, times(1)).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    void testUploadVideo_ShouldReturnS3Url_WhenUploadSuccessful() throws IOException {
        when(multipartFile.getOriginalFilename()).thenReturn("video.mp4");
        when(multipartFile.getContentType()).thenReturn("video/mp4");
        when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[10]));
        when(multipartFile.getSize()).thenReturn(10L);

        String result = s3FileService.uploadVideo(TEST_PATH, multipartFile);

        assertNotNull(result);
        assertTrue(result.startsWith("https://test-bucket.s3.us-east-1.amazonaws.com/"));
        verify(s3Client, times(1)).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    void testUploadFile_ShouldThrowIOException_WhenS3ExceptionOccursForUploadImage() throws IOException {
        when(multipartFile.getOriginalFilename()).thenReturn(TEST_FILE_NAME);
        when(multipartFile.getContentType()).thenReturn("image/png");
        when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[10]));
        when(multipartFile.getSize()).thenReturn(10L);

        S3Exception s3Exception = (S3Exception) S3Exception.builder()
                .message("S3 upload error")
                .awsErrorDetails(software.amazon.awssdk.awscore.exception.AwsErrorDetails.builder()
                        .errorMessage("Mocked AWS error message")  // Ensure awsErrorDetails is not null
                        .build())
                .build();

        doThrow(s3Exception).when(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));

        IOException exception = assertThrows(IOException.class, () -> s3FileService.uploadImage(TEST_PATH, multipartFile));

        assertTrue(exception.getMessage().contains("Error uploading file to S3: Mocked AWS error message"));
        verify(s3Client, times(1)).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    void testUploadFile_ShouldThrowIOException_WhenS3ExceptionOccursForUploadVideo() throws IOException {
        when(multipartFile.getOriginalFilename()).thenReturn(TEST_VIDEO_FILE_NAME);
        when(multipartFile.getContentType()).thenReturn("video/mp4");
        when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[10]));
        when(multipartFile.getSize()).thenReturn(10L);

        S3Exception s3Exception = (S3Exception) S3Exception.builder()
                .message("S3 upload error")
                .awsErrorDetails(software.amazon.awssdk.awscore.exception.AwsErrorDetails.builder()
                        .errorMessage("Mocked AWS error message")  // Ensure awsErrorDetails is not null
                        .build())
                .build();

        doThrow(s3Exception).when(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));

        IOException exception = assertThrows(IOException.class, () -> s3FileService.uploadVideo(TEST_PATH, multipartFile));

        assertTrue(exception.getMessage().contains("Error uploading file to S3: Mocked AWS error message"));
        verify(s3Client, times(1)).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    void testGetResource_ShouldThrowIOException_WhenFileDoesNotExist() {
        IOException exception = assertThrows(IOException.class, () ->
                s3FileService.getResource(TEST_PATH, "nonexistent.txt"));

        assertTrue(exception.getMessage().contains("Error fetching file from S3"));
    }

    @Test
    void testGenerateFileName_ShouldHandleEmptyImageFileName() throws IOException {
        when(multipartFile.getOriginalFilename()).thenReturn("");
        when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[10])); // ✅ Fix: Provide a non-null InputStream
        when(multipartFile.getSize()).thenReturn(10L); // ✅ Fix: Mock file size
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class))).thenReturn(PutObjectResponse.builder().build());

        String generatedFileName = s3FileService.uploadImage(TEST_PATH, multipartFile);

        assertNotNull(generatedFileName);
        assertTrue(generatedFileName.contains("file"));
    }

    @Test
    void testGenerateFileName_ShouldHandleEmptyVideoFileName() throws IOException {
        when(multipartFile.getOriginalFilename()).thenReturn("");
        when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[10])); // ✅ Fix: Provide a non-null InputStream
        when(multipartFile.getSize()).thenReturn(10L); // ✅ Fix: Mock file size
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class))).thenReturn(PutObjectResponse.builder().build());

        String generatedFileName = s3FileService.uploadVideo(TEST_PATH, multipartFile);

        assertNotNull(generatedFileName);
        assertTrue(generatedFileName.contains("file"));
    }

    @Test
    void testGenerateFileName_ShouldReplaceSpacesInImageFileName() throws IOException {
        when(multipartFile.getOriginalFilename()).thenReturn("my test file.jpg");
        when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[10])); // ✅ Mock InputStream
        when(multipartFile.getSize()).thenReturn(10L); // ✅ Mock Size

        String generatedFileName = s3FileService.uploadImage(TEST_PATH, multipartFile);

        // Debug output
        System.out.println("Generated File Name: " + generatedFileName);

        assertNotNull(generatedFileName);
        assertFalse(generatedFileName.contains(" "));
        assertTrue(generatedFileName.contains("my_test_file"));
    }

    @Test
    void testGenerateFileName_ShouldReplaceSpacesInVideoFileName() throws IOException {
        when(multipartFile.getOriginalFilename()).thenReturn("my test file.mp4");
        when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[10])); // ✅ Mock InputStream
        when(multipartFile.getSize()).thenReturn(10L); // ✅ Mock Size

        String generatedFileName = s3FileService.uploadVideo(TEST_PATH, multipartFile);

        // Debug output
        System.out.println("Generated File Name: " + generatedFileName);

        assertNotNull(generatedFileName);
        assertFalse(generatedFileName.contains(" "));
        assertTrue(generatedFileName.contains("my_test_file"));
    }

    @Test
    void testGetResource_ShouldReturnInputStream_WhenFileExists(@TempDir Path tempDir) throws IOException {
        // Simulate an S3 response by creating a local temporary file
        File tempFile = tempDir.resolve("test-file.txt").toFile();
        Files.write(tempFile.toPath(), "test content".getBytes());

        // Construct a valid S3 URL
        String path = "test-folder";
        String fileName = "test-file.txt";
        String s3Url = "https://" + BUCKET_NAME + ".s3." + REGION + ".amazonaws.com/" + path + "/" + fileName;

        // Mock the behavior of URL.openStream() using PowerMockito or a custom InputStream
        InputStream mockInputStream = new ByteArrayInputStream("test content".getBytes());

        try (MockedConstruction<URL> mockedUrl = mockConstruction(URL.class,
                (mock, context) -> when(mock.openStream()).thenReturn(mockInputStream))) {

            try (InputStream inputStream = s3FileService.getResource(path, fileName)) {
                assertNotNull(inputStream);
                assertEquals("test content", new String(inputStream.readAllBytes()));
            }
        }
    }
}