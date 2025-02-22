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

    @BeforeEach
    void setUp() {
        reactionRequest = new ReactionRequest();
        reactionRequest.setEmoji("👍");
    }

    @Test
    void test_reactToPost_whenValidRequest_returnSuccessMessage() {
        doNothing().when(reactionService).reactToPost(1L, "👍");

        ResponseEntity<APIResponse<String>> response = reactionController.reactToPost(1L, reactionRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().getSuccess());

        verify(reactionService, times(1)).reactToPost(1L, "👍");
    }

    @Test
    void test_getReactionCountsForPost_whenValidPostId_returnCounts() {
        ReactionSummaryResponse summary = new ReactionSummaryResponse();
        when(reactionService.getReactionCountsForPost(1L)).thenReturn(summary);

        ResponseEntity<APIResponse<ReactionSummaryResponse>> response = reactionController.getReactionCountsForPost(1L);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        verify(reactionService, times(1)).getReactionCountsForPost(1L);
    }
}