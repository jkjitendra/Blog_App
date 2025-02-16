package com.jk.blog.service;

import com.jk.blog.dto.reaction.ReactionSummaryResponse;
import com.jk.blog.entity.ReactionModel;
import com.jk.blog.entity.User;
import com.jk.blog.exception.ResourceNotFoundException;
import com.jk.blog.exception.UnAuthorizedException;
import com.jk.blog.repository.PostRepository;
import com.jk.blog.repository.mongo.ReactionRepository;
import com.jk.blog.service.impl.ReactionServiceImpl;
import com.jk.blog.utils.AuthUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReactionServiceImplTest {

    @InjectMocks
    private ReactionServiceImpl reactionService;

    @Mock
    private ReactionRepository reactionRepository;

    @Mock
    private PostRepository postRepository;

    private static final Long TEST_POST_ID = 1L;
    private static final Long TEST_COMMENT_ID = 10L;
    private static final Long TEST_USER_ID = 100L;
    private static final String TEST_EMOJI = "❤️";

    private User testUser;
    private ReactionModel testReaction;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUserId(TEST_USER_ID);

        testReaction = new ReactionModel();
        testReaction.setPostId(TEST_POST_ID);
        testReaction.setUserId(TEST_USER_ID);
        testReaction.setEmoji(TEST_EMOJI);
        testReaction.setType("post");
    }

    /** REACT TO POST TESTS **/

    @Test
    void test_reactToPost_whenUserNotAuthenticated_returnUnAuthorizedException() {
        try (MockedStatic<AuthUtil> mockedAuthUtil = Mockito.mockStatic(AuthUtil.class)) {
            mockedAuthUtil.when(AuthUtil::getAuthenticatedUser).thenReturn(null);

            assertThrows(UnAuthorizedException.class, () -> reactionService.reactToPost(TEST_POST_ID, TEST_EMOJI));
        }
    }

    @Test
    void test_reactToPost_whenPostNotFound_returnResourceNotFoundException() {
        try (MockedStatic<AuthUtil> mockedAuthUtil = Mockito.mockStatic(AuthUtil.class)) {
            mockedAuthUtil.when(AuthUtil::getAuthenticatedUser).thenReturn(testUser);
            when(postRepository.existsById(TEST_POST_ID)).thenReturn(false);

            assertThrows(ResourceNotFoundException.class, () -> reactionService.reactToPost(TEST_POST_ID, TEST_EMOJI));
        }
    }

    @Test
    void test_reactToPost_whenExistingReactionPresent_returnUpdatesReaction() {
        try (MockedStatic<AuthUtil> mockedAuthUtil = Mockito.mockStatic(AuthUtil.class)) {
            mockedAuthUtil.when(AuthUtil::getAuthenticatedUser).thenReturn(testUser);
            when(postRepository.existsById(TEST_POST_ID)).thenReturn(true);
            when(reactionRepository.findByUserIdAndPostId(TEST_USER_ID, TEST_POST_ID)).thenReturn(Optional.of(testReaction));

            reactionService.reactToPost(TEST_POST_ID, "😂");

            assertEquals("😂", testReaction.getEmoji());
            verify(reactionRepository, times(1)).save(testReaction);
        }
    }

    @Test
    void test_reactToPost_whenNoExistingReaction_returnCreatesReaction() {
        try (MockedStatic<AuthUtil> mockedAuthUtil = Mockito.mockStatic(AuthUtil.class)) {
            mockedAuthUtil.when(AuthUtil::getAuthenticatedUser).thenReturn(testUser);
            when(postRepository.existsById(TEST_POST_ID)).thenReturn(true);
            when(reactionRepository.findByUserIdAndPostId(TEST_USER_ID, TEST_POST_ID)).thenReturn(Optional.empty());

            reactionService.reactToPost(TEST_POST_ID, TEST_EMOJI);

            verify(reactionRepository, times(1)).save(any(ReactionModel.class));
        }
    }

    /** REACT TO COMMENT TESTS **/

    @Test
    void test_reactToComment_whenUserNotAuthenticated_returnUnAuthorizedException() {
        try (MockedStatic<AuthUtil> mockedAuthUtil = Mockito.mockStatic(AuthUtil.class)) {
            // Ensure that the post exists
            when(postRepository.existsById(TEST_POST_ID)).thenReturn(true);

            // Mock authentication failure
            mockedAuthUtil.when(AuthUtil::getAuthenticatedUser).thenReturn(null);

            assertThrows(UnAuthorizedException.class, () -> reactionService.reactToComment(TEST_POST_ID, TEST_COMMENT_ID, TEST_EMOJI));
        }
    }

    @Test
    void test_reactToComment_whenExistingReactionPresent_returnUpdatesReaction() {
        try (MockedStatic<AuthUtil> mockedAuthUtil = Mockito.mockStatic(AuthUtil.class)) {
            mockedAuthUtil.when(AuthUtil::getAuthenticatedUser).thenReturn(testUser);
            when(postRepository.existsById(TEST_POST_ID)).thenReturn(true);
            when(reactionRepository.findByUserIdAndCommentId(TEST_USER_ID, TEST_COMMENT_ID)).thenReturn(Optional.of(testReaction));

            reactionService.reactToComment(TEST_POST_ID, TEST_COMMENT_ID, "🔥");

            assertEquals("🔥", testReaction.getEmoji());
            verify(reactionRepository, times(1)).save(testReaction);
        }
    }

    @Test
    void test_reactToComment_whenNoExistingReaction_returnCreatesReaction() {
        try (MockedStatic<AuthUtil> mockedAuthUtil = Mockito.mockStatic(AuthUtil.class)) {
            mockedAuthUtil.when(AuthUtil::getAuthenticatedUser).thenReturn(testUser);
            when(postRepository.existsById(TEST_POST_ID)).thenReturn(true);
            when(reactionRepository.findByUserIdAndCommentId(TEST_USER_ID, TEST_COMMENT_ID)).thenReturn(Optional.empty());

            reactionService.reactToComment(TEST_POST_ID, TEST_COMMENT_ID, TEST_EMOJI);

            verify(reactionRepository, times(1)).save(any(ReactionModel.class));
        }
    }

    /** FIXED THIS TEST (REMOVED THE EXTRA STATIC MOCKING) **/
    @Test
    void test_reactToComment_whenPostNotFound_returnResourceNotFoundException() {
        try (MockedStatic<AuthUtil> mockedAuthUtil = Mockito.mockStatic(AuthUtil.class)) {
            mockedAuthUtil.when(AuthUtil::getAuthenticatedUser).thenReturn(testUser);
            when(postRepository.existsById(TEST_POST_ID)).thenReturn(false);

            assertThrows(ResourceNotFoundException.class, () -> reactionService.reactToComment(TEST_POST_ID, TEST_COMMENT_ID, TEST_EMOJI));
        }
    }

    /** GET REACTION COUNTS TESTS **/

    @Test
    void test_getReactionCountsForPost_whenPostHasReactions_returnReactionSummary() {
        List<Map<String, Object>> reactionData = List.of(
                Map.of("_id", "😂", "count", 5),
                Map.of("_id", "❤️", "count", 3)
        );
        when(reactionRepository.getReactionCountsByPostId(TEST_POST_ID)).thenReturn(reactionData);

        ReactionSummaryResponse response = reactionService.getReactionCountsForPost(TEST_POST_ID);

        assertEquals(2, response.getEmojis().size());
        assertEquals(8, response.getTotalReactions());
    }

    @Test
    void test_getReactionCountsForComment_whenCommentHasReactions_returnReactionSummary() {
        List<Map<String, Object>> reactionData = List.of(
                Map.of("_id", "🔥", "count", 4),
                Map.of("_id", "😆", "count", 2)
        );
        when(reactionRepository.getReactionCountsByCommentId(TEST_COMMENT_ID)).thenReturn(reactionData);

        ReactionSummaryResponse response = reactionService.getReactionCountsForComment(TEST_COMMENT_ID);

        assertEquals(2, response.getEmojis().size());
        assertEquals(6, response.getTotalReactions());
    }
}