package com.jk.blog.controller;

import com.jk.blog.dto.APIResponse;
import com.jk.blog.dto.reaction.ReactionRequest;
import com.jk.blog.dto.reaction.ReactionSummaryResponse;
import com.jk.blog.service.ReactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReactionControllerTest {

    @InjectMocks
    private ReactionController reactionController;

    @Mock
    private ReactionService reactionService;

    private ReactionRequest reactionRequest;

    private ReactionRequest validReactionRequest;
    private ReactionRequest invalidReactionRequest;

    @BeforeEach
    void setUp() {
        validReactionRequest = new ReactionRequest();
        validReactionRequest.setEmoji("👍"); // Valid emoji reaction

        invalidReactionRequest = new ReactionRequest(); // No emoji set
    }


//    @Test
//    void test_reactToPost_whenValidRequest_returnSuccessMessage() {
//        doNothing().when(reactionService).reactToPost(1L, "👍");
//
//        ResponseEntity<APIResponse<String>> response = reactionController.reactToPost(1L, reactionRequest);
//
//        assertNotNull(response);
//        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
//        assertTrue(response.getBody().getSuccess());
//
//        verify(reactionService, times(1)).reactToPost(1L, "👍");
//    }

    @Test
    void test_getReactionCountsForPost_whenValidPostId_returnCounts() {
        ReactionSummaryResponse summary = new ReactionSummaryResponse();
        when(reactionService.getReactionCountsForPost(1L)).thenReturn(summary);

        ResponseEntity<APIResponse<ReactionSummaryResponse>> response = reactionController.getReactionCountsForPost(1L);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        verify(reactionService, times(1)).getReactionCountsForPost(1L);
    }

    @Test
    void test_reactToComment_whenValidReactionProvided_returnSuccess() {
        // Given
        Long postId = 1L;
        Long commentId = 1L;

        doNothing().when(reactionService).reactToComment(postId, commentId, validReactionRequest.getEmoji());

        // When
        ResponseEntity<APIResponse<String>> response = reactionController.reactToComment(postId, commentId, validReactionRequest);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().getSuccess());
        assertEquals("Reaction added/updated successfully", response.getBody().getMessage());

        verify(reactionService, times(1)).reactToComment(postId, commentId, validReactionRequest.getEmoji());
    }

    @Test
    void test_reactToComment_whenInvalidReactionProvided_returnBadRequest() {
        // Given
        Long postId = 1L;
        Long commentId = 1L;

        // When
        ResponseEntity<APIResponse<String>> response = reactionController.reactToComment(postId, commentId, invalidReactionRequest);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse(response.getBody().getSuccess());
        assertEquals("Invalid reaction data", response.getBody().getMessage());

        verify(reactionService, never()).reactToComment(anyLong(), anyLong(), anyString());
    }

    @Test
    void test_reactToComment_whenNullReactionRequestProvided_returnBadRequest() {
        // Given
        Long postId = 1L;
        Long commentId = 1L;

        // When
        ResponseEntity<APIResponse<String>> response = reactionController.reactToComment(postId, commentId, null);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse(response.getBody().getSuccess());
        assertEquals("Invalid reaction data", response.getBody().getMessage());

        verify(reactionService, never()).reactToComment(anyLong(), anyLong(), anyString());
    }
}