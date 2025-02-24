package com.jk.blog.controller;

import com.jk.blog.dto.APIResponse;
import com.jk.blog.dto.PageableResponse;
import com.jk.blog.dto.post.PostRequestBody;
import com.jk.blog.dto.post.PostResponseBody;
import com.jk.blog.service.FileService;
import com.jk.blog.service.PostService;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostControllerTest {

    @Mock
    private PostService postService;

    @Mock
    private FileService fileService;

    @InjectMocks
    private PostController postController;

    private PostResponseBody postResponseBody;

    @BeforeEach
    void setUp() {
        postResponseBody = new PostResponseBody();
        postResponseBody.setPostId(1L);
    }

    @Test
    void test_createPost_WhenValidRequest_returnSuccess() throws IOException {
        PostRequestBody postRequestBody = new PostRequestBody();
        MultipartFile image = mock(MultipartFile.class);
        MultipartFile video = mock(MultipartFile.class);
        when(postService.createPost(any(), any(), any())).thenReturn(postResponseBody);

        ResponseEntity<APIResponse<PostResponseBody>> response = postController.createPost("{}", image, video);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertTrue(response.getBody().getSuccess());
        assertEquals("Post created successfully", response.getBody().getMessage());
    }

    @Test
    void test_getPostById_WhenPostExists_returnPostDetails() {
        when(postService.getPostById(1L)).thenReturn(postResponseBody);

        ResponseEntity<APIResponse<PostResponseBody>> response = postController.getPostById(1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().getSuccess());
        assertEquals("Post fetched successfully", response.getBody().getMessage());
    }

    @Test
    void test_getAllPost_WhenPostsExist_returnPostList() {
        PageableResponse<PostResponseBody> pageableResponse = new PageableResponse<>();
        when(postService.getAllPost(anyInt(), anyInt(), anyString(), anyString())).thenReturn(pageableResponse);

        ResponseEntity<APIResponse<PageableResponse<PostResponseBody>>> response = postController.getAllPost(1, 10, "createdAt", "desc");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().getSuccess());
        assertEquals("Posts fetched successfully", response.getBody().getMessage());
    }

    @Test
    void test_deletePost_WhenPostExists_returnSuccess() {
        doNothing().when(postService).deletePost(1L);

        ResponseEntity<APIResponse<Void>> response = postController.deletePost(1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().getSuccess());
        assertEquals("Post Deleted Successfully", response.getBody().getMessage());
    }

    @Test
    void test_togglePostVisibility_WhenVisibilityUpdated_returnSuccess() {
        when(postService.togglePostVisibility(1L, true)).thenReturn(postResponseBody);
        Map<String, Boolean> visibility = Map.of("isLive", true);

        ResponseEntity<APIResponse<Void>> response = postController.togglePostVisibility(1L, visibility);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().getSuccess());
        assertEquals("isLive updated Successfully", response.getBody().getMessage());
    }

    @Test
    void test_updatePost_WhenValidRequest_returnSuccess() throws IOException {
        MultipartFile image = mock(MultipartFile.class);
        MultipartFile video = mock(MultipartFile.class);
        when(postService.updatePost(anyLong(), any(), any(), any())).thenReturn(postResponseBody);

        ResponseEntity<APIResponse<Void>> response = postController.updatePost(1L, "{}", image, video);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().getSuccess());
        assertEquals("Post updated successfully", response.getBody().getMessage());
    }

    @Test
    void test_patchPost_WhenValidRequest_returnSuccess() throws IOException {
        MultipartFile image = mock(MultipartFile.class);
        MultipartFile video = mock(MultipartFile.class);
        when(postService.patchPost(anyLong(), any(), any(), any())).thenReturn(postResponseBody);

        ResponseEntity<APIResponse<Void>> response = postController.patchPost(1L, "{}", image, video);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().getSuccess());
        assertEquals("Post updated successfully", response.getBody().getMessage());
    }

    @Test
    void test_setExistingPostAsMemberPost_WhenValidRequest_returnSuccess() {
        when(postService.setAsMemberPost(1L)).thenReturn(postResponseBody);

        ResponseEntity<APIResponse<PostResponseBody>> response = postController.setExistingPostAsMemberPost(1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().getSuccess());
        assertEquals("Post marked as a member post successfully", response.getBody().getMessage());
    }

    @Test
    void test_archivePost_WhenValidRequest_returnSuccess() {
        when(postService.archivePost(1L)).thenReturn(postResponseBody);

        ResponseEntity<APIResponse<PostResponseBody>> response = postController.archivePost(1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().getSuccess());
        assertEquals("Post archived successfully", response.getBody().getMessage());
    }

    @Test
    void test_getArchivedPosts_WhenArchivedPostsExist_returnSuccess() {
        PageableResponse<PostResponseBody> pageableResponse = new PageableResponse<>();
        when(postService.getArchivedPosts(anyInt(), anyInt(), anyString(), anyString())).thenReturn(pageableResponse);

        ResponseEntity<APIResponse<PageableResponse<PostResponseBody>>> response = postController.getArchivedPosts(1, 10, "createdAt", "desc");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().getSuccess());
        assertEquals("Archived posts fetched successfully", response.getBody().getMessage());
    }

    @Test
    void test_unarchivePost_WhenValidRequest_returnSuccess() {
        when(postService.unarchivePost(1L)).thenReturn(postResponseBody);

        ResponseEntity<APIResponse<PostResponseBody>> response = postController.unarchivePost(1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().getSuccess());
        assertEquals("Post unarchived successfully", response.getBody().getMessage());
    }

    @Test
    void test_patchPostDeactivate_WhenValidRequest_returnSuccess() throws IOException {
        when(postService.deactivatePost(1L)).thenReturn(postResponseBody);

        ResponseEntity<APIResponse<PostResponseBody>> response = postController.patchPostDeactivate(1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().getSuccess());
        assertEquals("Post Deactivated Successfully", response.getBody().getMessage());
    }

    @Test
    void test_patchPostActivate_WhenValidRequest_returnSuccess() throws IOException {
        when(postService.activatePost(1L)).thenReturn(postResponseBody);

        ResponseEntity<APIResponse<PostResponseBody>> response = postController.patchPostActivate(1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().getSuccess());
        assertEquals("Post Activated Successfully", response.getBody().getMessage());
    }

    @Test
    void test_getPostsByUser_WhenValidRequest_returnSuccess() {
        PageableResponse<PostResponseBody> pageableResponse = new PageableResponse<>();
        when(postService.getPostsByUser(anyString(), anyInt(), anyInt(), anyString(), anyString())).thenReturn(pageableResponse);

        ResponseEntity<APIResponse<PageableResponse<PostResponseBody>>> response = postController.getPostsByUser("testUser", 1, 10, "createdAt", "desc");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().getSuccess());
        assertEquals("User's posts fetched successfully", response.getBody().getMessage());
    }

    @Test
    void test_getPostsByCategory_WhenValidRequest_returnSuccess() {
        PageableResponse<PostResponseBody> pageableResponse = new PageableResponse<>();
        when(postService.getPostsByCategory(anyLong(), anyInt(), anyInt(), anyString(), anyString())).thenReturn(pageableResponse);

        ResponseEntity<APIResponse<PageableResponse<PostResponseBody>>> response = postController.getPostsByCategory(1L, 1, 10, "createdAt", "desc");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().getSuccess());
        assertEquals("Category posts fetched successfully", response.getBody().getMessage());
    }

    @Test
    void test_uploadPostImage_WhenValidRequest_returnSuccess() throws IOException {
        MultipartFile image = mock(MultipartFile.class);
        when(fileService.uploadImage(anyString(), any())).thenReturn("image.jpg");

        ResponseEntity<APIResponse<String>> response = postController.uploadPostImage(image);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().getSuccess());
        assertEquals("Image uploaded successfully", response.getBody().getMessage());
    }

    @Test
    void test_uploadPostVideo_WhenValidRequest_returnSuccess() throws IOException {
        MultipartFile video = mock(MultipartFile.class);
        when(fileService.uploadVideo(anyString(), any())).thenReturn("video.mp4");

        ResponseEntity<APIResponse<String>> response = postController.uploadPostVideo(video);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().getSuccess());
        assertEquals("Video uploaded successfully", response.getBody().getMessage());
    }

    @Test
    void test_getPostsByTitleSearch_WhenValidRequest_returnSuccess() {
        PageableResponse<PostResponseBody> pageableResponse = new PageableResponse<>();
        when(postService.searchPostsByTitle(anyString(), anyInt(), anyInt(), anyString(), anyString())).thenReturn(pageableResponse);

        ResponseEntity<APIResponse<PageableResponse<PostResponseBody>>> response = postController.getPostsByTitleSearch("test", 1, 10, "createdAt", "desc");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().getSuccess());
        assertEquals("Posts fetched successfully", response.getBody().getMessage());
    }

    @Test
    void test_downloadImage_WhenValidRequest_returnImage() throws IOException {
        InputStream inputStream = mock(InputStream.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        ServletOutputStream outputStream = mock(ServletOutputStream.class);

        when(fileService.getResource(anyString(), anyString())).thenReturn(inputStream);
        when(response.getOutputStream()).thenReturn(outputStream);

        postController.downloadImage("testImage.jpg", response);

        verify(fileService, times(1)).getResource(anyString(), eq("testImage.jpg"));
        verify(response, times(1)).setContentType(eq("image/jpeg"));
        verify(response, times(1)).getOutputStream();
    }
}