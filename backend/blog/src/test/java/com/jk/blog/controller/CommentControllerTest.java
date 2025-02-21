package com.jk.blog.controller;

import com.jk.blog.dto.APIResponse;
import com.jk.blog.dto.comment.CommentRequestBody;
import com.jk.blog.dto.comment.CommentResponseBody;
import com.jk.blog.service.CommentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentControllerTest {

    @InjectMocks
    private CommentController commentController;

    @Mock
    private CommentService commentService;

    private CommentResponseBody commentResponseBody;
    private CommentRequestBody commentRequestBody;

    @BeforeEach
    void setUp() {
        commentRequestBody = new CommentRequestBody();
        commentRequestBody.setCommentDesc("This is a test comment");

        commentResponseBody = new CommentResponseBody();
        commentResponseBody.setCommentId(1L);
        commentResponseBody.setCommentDesc("This is a test comment");
    }

    @Test
    void test_createComment_whenValidRequest_returnCreatedComment() {
        when(commentService.createComment(any(CommentRequestBody.class), anyLong())).thenReturn(commentResponseBody);

        ResponseEntity<APIResponse<CommentResponseBody>> response = commentController.createComment(commentRequestBody, 1L);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertTrue(response.getBody().getSuccess());
        assertEquals("This is a test comment", response.getBody().getData().getCommentDesc());

        verify(commentService, times(1)).createComment(any(CommentRequestBody.class), eq(1L));
    }

    @Test
    void test_getAllComment_whenValidPostId_returnCommentList() {
        List<CommentResponseBody> commentList = Arrays.asList(commentResponseBody, new CommentResponseBody());
        when(commentService.getAllComments(1L)).thenReturn(commentList);

        ResponseEntity<APIResponse<List<CommentResponseBody>>> response = commentController.getAllComment(1L);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().getSuccess());
        assertEquals(2, response.getBody().getData().size());

        verify(commentService, times(1)).getAllComments(1L);
    }

    @Test
    void test_getCommentById_whenValidCommentId_returnComment() {
        when(commentService.getCommentById(1L)).thenReturn(commentResponseBody);

        ResponseEntity<APIResponse<CommentResponseBody>> response = commentController.getCommentById(1L);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().getSuccess());
        assertEquals(1L, response.getBody().getData().getCommentId());

        verify(commentService, times(1)).getCommentById(1L);
    }

    @Test
    void test_updateComment_whenValidRequest_returnUpdatedComment() throws IOException {
        when(commentService.updateComment(any(CommentRequestBody.class), anyLong())).thenReturn(commentResponseBody);

        ResponseEntity<APIResponse<CommentResponseBody>> response = commentController.updateComment(commentRequestBody, 1L);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().getSuccess());
        assertEquals("This is a test comment", response.getBody().getData().getCommentDesc());

        verify(commentService, times(1)).updateComment(any(CommentRequestBody.class), eq(1L));
    }

    @Test
    void test_deleteComment_whenValidCommentId_returnSuccessMessage() {
        doNothing().when(commentService).deleteComment(1L);

        ResponseEntity<APIResponse<String>> response = commentController.deleteComment(1L);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().getSuccess());

        verify(commentService, times(1)).deleteComment(1L);
    }

    @Test
    void test_deleteMultipleComments_whenValidPostId_returnSuccessMessage() {
        doNothing().when(commentService).deleteMultipleComments(eq(1L), anyList());

        ResponseEntity<APIResponse<String>> response = commentController.deleteMultipleComments(1L, Arrays.asList(1L, 2L));

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().getSuccess());

        verify(commentService, times(1)).deleteMultipleComments(eq(1L), anyList());
    }

    @Test
    void test_patchCommentDeactivate_whenValidCommentId_returnSuccessMessage() throws IOException {
        doNothing().when(commentService).deactivateComment(1L);

        ResponseEntity<APIResponse<String>> response = commentController.patchCommentDeactivate(1L);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().getSuccess());

        verify(commentService, times(1)).deactivateComment(1L);
    }

    @Test
    void test_patchCommentActivate_whenValidCommentId_returnSuccessMessage() throws IOException {
        doNothing().when(commentService).activateComment(1L);

        ResponseEntity<APIResponse<String>> response = commentController.patchCommentActivate(1L);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().getSuccess());

        verify(commentService, times(1)).activateComment(1L);
    }
}