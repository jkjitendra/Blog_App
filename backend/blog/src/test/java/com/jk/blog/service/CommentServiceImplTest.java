package com.jk.blog.service;

import com.jk.blog.dto.AuthDTO.AuthenticatedUserDTO;
import com.jk.blog.dto.comment.CommentRequestBody;
import com.jk.blog.dto.comment.CommentResponseBody;
import com.jk.blog.entity.Comment;
import com.jk.blog.entity.Post;
import com.jk.blog.entity.User;
import com.jk.blog.exception.ResourceNotFoundException;
import com.jk.blog.exception.UnAuthorizedException;
import com.jk.blog.repository.CommentRepository;
import com.jk.blog.repository.PostRepository;
import com.jk.blog.service.impl.CommentServiceImpl;
import com.jk.blog.utils.AuthUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceImplTest {

    @InjectMocks
    private CommentServiceImpl commentService;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private AuthUtil authUtil;

    private User testUser;
    private Post testPost;
    private Comment testComment;
    private AuthenticatedUserDTO authenticatedUserDTO;

    private static final Long TEST_USER_ID = 1L;
    private static final Long TEST_POST_ID = 100L;
    private static final Long TEST_COMMENT_ID = 200L;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUserId(TEST_USER_ID);

        testPost = new Post();
        testPost.setPostId(TEST_POST_ID);
        testPost.setUser(testUser);

        testComment = new Comment();
        testComment.setCommentId(TEST_COMMENT_ID);
        testComment.setUser(testUser);
        testComment.setPost(testPost);
        testComment.setCommentCreatedDate(Instant.now());

        authenticatedUserDTO = new AuthenticatedUserDTO();
        authenticatedUserDTO.setOAuthUser(true);
        authenticatedUserDTO.setProvider("Github");
        authenticatedUserDTO.setEmail("testuser@github.com");
        authenticatedUserDTO.setUser(testUser);
    }

    /** CREATE COMMENT TESTS **/

    @Test
    void test_createComment_WhenUserAuthenticated_ReturnCommentResponseBody() {
        CommentRequestBody requestBody = new CommentRequestBody();
        requestBody.setCommentDesc("This is a test comment");

        when(authUtil.getAuthenticatedUser()).thenReturn(authenticatedUserDTO);

        when(postRepository.findById(TEST_POST_ID)).thenReturn(Optional.of(testPost));
        when(commentRepository.save(any(Comment.class))).thenReturn(testComment);
        when(modelMapper.map(testComment, CommentResponseBody.class)).thenReturn(new CommentResponseBody());

        CommentResponseBody response = commentService.createComment(requestBody, TEST_POST_ID);

        assertNotNull(response);
        verify(commentRepository, times(1)).save(any(Comment.class));

    }

    @Test
    void test_createComment_WhenUserNotAuthenticated_ThrowUnAuthorizedException() {
        CommentRequestBody requestBody = new CommentRequestBody();
        requestBody.setCommentDesc("Test comment");

        when(authUtil.getAuthenticatedUser()).thenReturn(null);

        assertThrows(UnAuthorizedException.class, () -> commentService.createComment(requestBody, TEST_POST_ID));

    }

    /** GET COMMENTS TESTS **/

    @Test
    void test_getAllComments_WhenPostHasComments_ReturnListOfComments() {
        when(commentRepository.findByPost_PostId(TEST_POST_ID)).thenReturn(Optional.of(List.of(testComment)));
        when(modelMapper.map(testComment, CommentResponseBody.class)).thenReturn(new CommentResponseBody());

        List<CommentResponseBody> response = commentService.getAllComments(TEST_POST_ID);

        assertFalse(response.isEmpty());
        verify(commentRepository, times(1)).findByPost_PostId(TEST_POST_ID);
    }

    @Test
    void test_getAllComments_WhenNoCommentsFound_ThrowResourceNotFoundException() {
        when(commentRepository.findByPost_PostId(TEST_POST_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> commentService.getAllComments(TEST_POST_ID));
    }

    /** GET COMMENTS By Id TESTS **/

    @Test
    void test_getCommentById_WhenPostHasComments_ReturnListOfComments() {
        when(commentRepository.findById(TEST_COMMENT_ID)).thenReturn(Optional.of(testComment));
        when(modelMapper.map(testComment, CommentResponseBody.class)).thenReturn(new CommentResponseBody());

        CommentResponseBody response = commentService.getCommentById(TEST_COMMENT_ID);

        assertNotNull(response);
        verify(commentRepository, times(1)).findById(TEST_COMMENT_ID);
    }

    @Test
    void test_getCommentById_WhenNoCommentsFound_ThrowResourceNotFoundException() {
        when(commentRepository.findById(TEST_COMMENT_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> commentService.getCommentById(TEST_COMMENT_ID));
    }

    /** GET COMMENTS By Id TESTS **/

    @Test
    void test_getCommentUserId_WhenPostHasComments_ReturnListOfComments() {
        when(commentRepository.findById(TEST_COMMENT_ID)).thenReturn(Optional.of(testComment));

        Long response = commentService.getCommentUserId(TEST_COMMENT_ID);

        assertNotNull(response);
        verify(commentRepository, times(1)).findById(TEST_COMMENT_ID);
    }

    @Test
    void test_getCommentUserId_WhenNoCommentsFound_ThrowResourceNotFoundException() {
        when(commentRepository.findById(TEST_COMMENT_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> commentService.getCommentUserId(TEST_COMMENT_ID));
    }

    /** UPDATE COMMENT TESTS **/

    @Test
    void test_updateComment_WhenUserIsOwner_ReturnUpdatedComment() {
        CommentRequestBody requestBody = new CommentRequestBody();
        requestBody.setCommentDesc("Updated comment");

        when(authUtil.getAuthenticatedUser()).thenReturn(authenticatedUserDTO);

        when(commentRepository.findById(TEST_COMMENT_ID)).thenReturn(Optional.of(testComment));
        when(commentRepository.save(any(Comment.class))).thenReturn(testComment);
        when(modelMapper.map(testComment, CommentResponseBody.class)).thenReturn(new CommentResponseBody());

        CommentResponseBody response = commentService.updateComment(requestBody, TEST_COMMENT_ID);

        assertNotNull(response);
        verify(commentRepository, times(1)).save(any(Comment.class));

    }

    @Test
    void test_updateComment_WhenUserIsNotOwner_ThrowUnAuthorizedException() {
        CommentRequestBody requestBody = new CommentRequestBody();
        requestBody.setCommentDesc("Updated comment");

        User differentUser = new User();
        differentUser.setUserId(2L);
        authenticatedUserDTO.setUser(differentUser);
        when(authUtil.getAuthenticatedUser()).thenReturn(authenticatedUserDTO);

        when(commentRepository.findById(TEST_COMMENT_ID)).thenReturn(Optional.of(testComment));

        assertThrows(UnAuthorizedException.class, () -> commentService.updateComment(requestBody, TEST_COMMENT_ID));

    }

    /** DELETE COMMENT TESTS **/

    @Test
    void test_deleteComment_WhenUserIsOwner_DeleteComment() {
        when(authUtil.getAuthenticatedUser()).thenReturn(authenticatedUserDTO);

        when(commentRepository.findById(TEST_COMMENT_ID)).thenReturn(Optional.of(testComment));
        doNothing().when(commentRepository).delete(testComment);

        commentService.deleteComment(TEST_COMMENT_ID);

        verify(commentRepository, times(1)).delete(testComment);

    }

    @Test
    void test_deleteComment_WhenUserIsNotOwner_ThrowUnAuthorizedException() {

        User differentUser = new User();
        differentUser.setUserId(2L);
        authenticatedUserDTO.setUser(differentUser);
        when(authUtil.getAuthenticatedUser()).thenReturn(authenticatedUserDTO);

        when(commentRepository.findById(TEST_COMMENT_ID)).thenReturn(Optional.of(testComment));

        assertThrows(UnAuthorizedException.class, () -> commentService.deleteComment(TEST_COMMENT_ID));

    }

    /** Delete Multiple Comments **/

    @Test
    void test_deleteMultipleComments_WhenUserHasPermission_DeleteComments() {
        List<Long> commentIds = List.of(201L, 202L);

        Comment comment1 = new Comment();
        comment1.setCommentId(201L);
        comment1.setPost(testPost);
        comment1.setUser(testUser);

        Comment comment2 = new Comment();
        comment2.setCommentId(202L);
        comment2.setPost(testPost);
        comment2.setUser(testUser);

        when(authUtil.getAuthenticatedUser()).thenReturn(authenticatedUserDTO);

        when(commentRepository.findById(201L)).thenReturn(Optional.of(comment1));
        when(commentRepository.findById(202L)).thenReturn(Optional.of(comment2));

        doNothing().when(commentRepository).delete(any(Comment.class));

        commentService.deleteMultipleComments(TEST_POST_ID, commentIds);

        verify(commentRepository, times(2)).delete(any(Comment.class));

    }

    @Test
    void test_deleteMultipleComments_WhenCommentNotBelongToPost_ThrowUnAuthorizedException() {
        List<Long> commentIds = List.of(201L);

        Post otherPost = new Post();
        otherPost.setPostId(999L);

        Comment comment = new Comment();
        comment.setCommentId(201L);
        comment.setPost(otherPost);
        comment.setUser(testUser);

        when(commentRepository.findById(201L)).thenReturn(Optional.of(comment));

        assertThrows(UnAuthorizedException.class, () -> commentService.deleteMultipleComments(TEST_POST_ID, commentIds));
    }

    @Test
    void test_deleteMultipleComments_WhenUserUnauthorized_ThrowUnAuthorizedException() {
        List<Long> commentIds = List.of(201L);

        User differentUser = new User();
        differentUser.setUserId(999L);

        Comment comment = new Comment();
        comment.setCommentId(201L);
        comment.setPost(testPost);
        comment.setUser(differentUser);

        when(authUtil.getAuthenticatedUser()).thenReturn(authenticatedUserDTO);

        when(commentRepository.findById(201L)).thenReturn(Optional.of(comment));

        assertThrows(UnAuthorizedException.class, () -> commentService.deleteMultipleComments(TEST_POST_ID, commentIds));

    }

    /** Can Delete Comment **/

    @Test
    void test_canDeleteComment_WhenUserIsAdmin_ReturnTrue() {
        when(authUtil.getAuthenticatedUser()).thenReturn(authenticatedUserDTO);
        when(authUtil.userHasRole(testUser, "ROLE_ADMIN")).thenReturn(true);

        when(commentRepository.findById(TEST_COMMENT_ID)).thenReturn(Optional.of(testComment));

        boolean result = commentService.canDeleteComment(TEST_USER_ID, TEST_COMMENT_ID);

        assertTrue(result);

    }

    @Test
    void test_canDeleteComment_WhenUserIsModerator_ReturnTrue() {
        when(authUtil.getAuthenticatedUser()).thenReturn(authenticatedUserDTO);
        when(authUtil.userHasRole(testUser, "ROLE_ADMIN")).thenReturn(true);

        when(commentRepository.findById(TEST_COMMENT_ID)).thenReturn(Optional.of(testComment));

        boolean result = commentService.canDeleteComment(TEST_USER_ID, TEST_COMMENT_ID);

        assertTrue(result);

    }

    @Test
    void test_canDeleteComment_WhenUserIsPostOwner_ReturnTrue() {
        testPost.setUser(testUser);
        testComment.setPost(testPost);

        when(authUtil.getAuthenticatedUser()).thenReturn(authenticatedUserDTO);

        when(commentRepository.findById(TEST_COMMENT_ID)).thenReturn(Optional.of(testComment));

        boolean result = commentService.canDeleteComment(TEST_USER_ID, TEST_COMMENT_ID);

        assertTrue(result);

    }

    @Test
    void test_canDeleteComment_WhenUserIsCommentOwner_ReturnTrue() {
        testComment.setUser(testUser);

        when(authUtil.getAuthenticatedUser()).thenReturn(authenticatedUserDTO);

        when(commentRepository.findById(TEST_COMMENT_ID)).thenReturn(Optional.of(testComment));

        boolean result = commentService.canDeleteComment(TEST_USER_ID, TEST_COMMENT_ID);

        assertTrue(result);

    }

    @Test
    void test_canDeleteComment_WhenUserIsUnauthorized_ReturnFalse() {
        User differentUser = new User();
        differentUser.setUserId(999L);

        authenticatedUserDTO.setUser(differentUser);
        when(authUtil.getAuthenticatedUser()).thenReturn(authenticatedUserDTO);

        when(commentRepository.findById(TEST_COMMENT_ID)).thenReturn(Optional.of(testComment));

        boolean result = commentService.canDeleteComment(TEST_USER_ID, TEST_COMMENT_ID);

        assertFalse(result);

    }

    /** Can Bulk Delete **/

    @Test
    void test_canBulkDelete_WhenUserIsAdmin_ReturnTrue() {
        when(authUtil.getAuthenticatedUser()).thenReturn(authenticatedUserDTO);
        when(authUtil.userHasRole(testUser, "ROLE_ADMIN")).thenReturn(true);

        when(postRepository.findById(TEST_POST_ID)).thenReturn(Optional.of(testPost));

        boolean result = commentService.canBulkDelete(TEST_USER_ID, TEST_POST_ID);

        assertTrue(result);

    }

    @Test
    void test_canBulkDelete_WhenUserIsModerator_ReturnTrue() {
        when(authUtil.getAuthenticatedUser()).thenReturn(authenticatedUserDTO);
        when(authUtil.userHasRole(testUser, "ROLE_ADMIN")).thenReturn(true);

        when(postRepository.findById(TEST_POST_ID)).thenReturn(Optional.of(testPost));

        boolean result = commentService.canBulkDelete(TEST_USER_ID, TEST_POST_ID);

        assertTrue(result);

    }

    @Test
    void test_canBulkDelete_WhenUserIsPostOwner_ReturnTrue() {
        testPost.setUser(testUser);

        when(authUtil.getAuthenticatedUser()).thenReturn(authenticatedUserDTO);

        when(postRepository.findById(TEST_POST_ID)).thenReturn(Optional.of(testPost));

        boolean result = commentService.canBulkDelete(TEST_USER_ID, TEST_POST_ID);

        assertTrue(result);

    }

    @Test
    void test_canBulkDelete_WhenUserIsUnauthorized_ReturnFalse() {
        User differentUser = new User();
        differentUser.setUserId(999L);

        authenticatedUserDTO.setUser(differentUser);
        when(authUtil.getAuthenticatedUser()).thenReturn(authenticatedUserDTO);

        when(postRepository.findById(TEST_POST_ID)).thenReturn(Optional.of(testPost));

        boolean result = commentService.canBulkDelete(TEST_USER_ID, TEST_POST_ID);

        assertFalse(result);

    }

    /** DEACTIVATE COMMENT TESTS **/

    @Test
    void test_deactivateComment_WhenUserIsOwner_CommentDeactivated() {
        when(authUtil.getAuthenticatedUser()).thenReturn(authenticatedUserDTO);

        when(commentRepository.findById(TEST_COMMENT_ID)).thenReturn(Optional.of(testComment));

        commentService.deactivateComment(TEST_COMMENT_ID);

        assertTrue(testComment.isCommentDeleted());
        verify(commentRepository, times(1)).save(testComment);

    }

    @Test
    void test_deactivateComment_WhenUserIsNotOwner_ThrowUnAuthorizedException() {
        User differentUser = new User();
        differentUser.setUserId(2L);
        authenticatedUserDTO.setUser(differentUser);
        when(authUtil.getAuthenticatedUser()).thenReturn(authenticatedUserDTO);

        when(commentRepository.findById(TEST_COMMENT_ID)).thenReturn(Optional.of(testComment));

        assertThrows(UnAuthorizedException.class, () -> commentService.deactivateComment(TEST_COMMENT_ID));

    }

    /** ACTIVATE COMMENT TESTS **/

    @Test
    void test_activateComment_WhenWithinAllowedPeriod_CommentActivated() {
        testComment.setCommentDeleted(true);
        testComment.setCommentDeletionTimestamp(Instant.now().minus(10, java.time.temporal.ChronoUnit.DAYS));

        when(authUtil.getAuthenticatedUser()).thenReturn(authenticatedUserDTO);

        when(commentRepository.findById(TEST_COMMENT_ID)).thenReturn(Optional.of(testComment));

        commentService.activateComment(TEST_COMMENT_ID);

        assertFalse(testComment.isCommentDeleted());
        verify(commentRepository, times(1)).save(testComment);

    }

    @Test
    void test_activateComment_WhenBeyondAllowedPeriod_ThrowUnAuthorizedException() {
        testComment.setCommentDeleted(true);
        testComment.setCommentDeletionTimestamp(Instant.now().minus(100, ChronoUnit.DAYS));

        when(authUtil.getAuthenticatedUser()).thenReturn(authenticatedUserDTO); // Mock authenticated user

        when(commentRepository.findById(TEST_COMMENT_ID)).thenReturn(Optional.of(testComment));

        UnAuthorizedException thrownException = assertThrows(UnAuthorizedException.class,
                () -> commentService.activateComment(TEST_COMMENT_ID)
        );

        assertEquals("Comment cannot be activated as it is permanently deleted or outside the activation window.",
                thrownException.getMessage()
        );

    }
}