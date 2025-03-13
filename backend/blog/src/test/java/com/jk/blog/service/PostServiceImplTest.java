package com.jk.blog.service;

import com.jk.blog.dto.AuthDTO.AuthenticatedUserDTO;
import com.jk.blog.dto.PageableResponse;
import com.jk.blog.dto.comment.CommentResponseBody;
import com.jk.blog.dto.post.PostRequestBody;
import com.jk.blog.dto.post.PostResponseBody;
import com.jk.blog.entity.*;
import com.jk.blog.exception.InvalidPostStateException;
import com.jk.blog.exception.ResourceNotFoundException;
import com.jk.blog.exception.UnAuthorizedException;
import com.jk.blog.repository.*;
import com.jk.blog.security.AuthenticationFacade;
import com.jk.blog.service.impl.PostServiceImpl;
import com.jk.blog.utils.AuthUtil;
import com.jk.blog.utils.DateTimeUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceImplTest {

    @InjectMocks
    private PostServiceImpl postService;

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private TagRepository tagRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private AuthenticationFacade authenticationFacade;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private FileService fileService;
    
    @Mock
    private AuthUtil authUtil;

    @Mock
    private MultipartFile image;

    @Mock
    private MultipartFile video;

    private User testUser;
    private Post testPost;
    private PostRequestBody postRequestBody;
    private Category testCategory;
    private Set<Tag> testTags;
    private AuthenticatedUserDTO authenticatedUserDTO;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUserId(1L);
        testUser.setUserName("testUser");
        testUser.setEmail("testuser@github.com");

        testCategory = new Category();
        testCategory.setCategoryId(1L);
        testCategory.setCategoryTitle("Tech");

        testTags = new HashSet<>();
        testTags.add(new Tag("Java"));

        testPost = new Post();
        testPost.setPostId(1L);
        testPost.setPostTitle("Test Post");
        testPost.setPostContent("This is a test post");
        testPost.setUser(testUser);
        testPost.setCategory(testCategory);
        testPost.setTags(testTags);
        testPost.setLive(true);
        testPost.setArchived(false);
        testPost.setMemberPost(true);
        testPost.setPostCreatedDate(Instant.now());

        postRequestBody = new PostRequestBody();
        postRequestBody.setTitle("New Post");
        postRequestBody.setContent("New Post Content");
        postRequestBody.setCategoryId(1L);
        postRequestBody.setTagNames(Set.of("Java"));
        postRequestBody.setIsMemberPost(true);

        authenticatedUserDTO = new AuthenticatedUserDTO();
        authenticatedUserDTO.setOAuthUser(true);
        authenticatedUserDTO.setProvider("Github");
        authenticatedUserDTO.setEmail("testuser@github.com");
        authenticatedUserDTO.setRoles(Set.of("ROLE_SUBSCRIBER"));
        authenticatedUserDTO.setUser(testUser);

    }

    @Test
    void test_createPost_WhenValidInput_ReturnPostResponseBody() throws IOException {
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.of(testCategory));
        when(authenticationFacade.hasAnyRole("ROLE_SUBSCRIBER", "ROLE_MODERATOR", "ROLE_ADMIN")).thenReturn(true);
        when(postRepository.save(any(Post.class))).thenReturn(testPost);
        when(fileService.uploadImage(anyString(), any(MultipartFile.class))).thenReturn("test-image.jpg");
        when(fileService.uploadVideo(anyString(), any(MultipartFile.class))).thenReturn("test-video.mp4");

        when(authUtil.getAuthenticatedUser()).thenReturn(authenticatedUserDTO);

        PostResponseBody response = postService.createPost(postRequestBody, image, video);
        assertNotNull(response);
        assertEquals("Test Post", response.getTitle());
        verify(postRepository, times(1)).save(any(Post.class));

    }

    @Test
    void test_createPost_WhenAuthenticatedUserIsNull_ShouldThrowUnAuthorizedException() {

        when(authUtil.getAuthenticatedUser()).thenReturn(null);

        assertThrows(UnAuthorizedException.class, () -> postService.createPost(postRequestBody, image, video));
        verify(postRepository, never()).save(any(Post.class));

    }

    @Test
    void test_getPostById_WhenPostExists_ReturnPostResponseBody() {
        testPost.setMemberPost(false);
        when(postRepository.findByPostIdAndIsLiveTrueAndIsPostDeletedFalse(anyLong()))
                .thenReturn(Optional.of(testPost));

        PostResponseBody response = postService.getPostById(1L);

        assertNotNull(response);
        assertEquals("Test Post", response.getTitle());
    }

    @Test
    void test_getPostById_whenPostIsMemberPostAndUserNotAuthorized_throwUnAuthorizedException() {
        // Arrange
        testPost.setMemberPost(true); // Mark the post as a member-only post

        when(postRepository.findByPostIdAndIsLiveTrueAndIsPostDeletedFalse(anyLong()))
                .thenReturn(Optional.of(testPost));
        when(authenticationFacade.hasAnyRole("ROLE_SUBSCRIBER", "ROLE_MODERATOR", "ROLE_ADMIN"))
                .thenReturn(false); // Mock user without required roles

        assertThrows(UnAuthorizedException.class, () -> postService.getPostById(1L));

        verify(postRepository, times(1)).findByPostIdAndIsLiveTrueAndIsPostDeletedFalse(anyLong());
        verify(authenticationFacade, times(1)).hasAnyRole("ROLE_SUBSCRIBER", "ROLE_MODERATOR", "ROLE_ADMIN");
    }

    @Test
    void test_getPostById_WhenPostNotFound_ShouldThrowResourceNotFoundException() {
        when(postRepository.findByPostIdAndIsLiveTrueAndIsPostDeletedFalse(anyLong()))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> postService.getPostById(1L));
    }

    @Test
    void test_getAllPost_WhenRoleIsSUBSCRIBER_ReturnPageableResponse() {
        Page<Post> page = new PageImpl<>(Collections.singletonList(testPost));
        Comment comment = new Comment();
        comment.setCommentId(1L);
        comment.setCommentDesc("Awesome Post");
        comment.setCommentDeleted(false);
        testPost.setComments(List.of(comment));

        CommentResponseBody commentResponseBody = new CommentResponseBody();
        commentResponseBody.setCommentId(1L);
        commentResponseBody.setCommentDesc("Awesome Post");
        commentResponseBody.setCommentCreatedDate(DateTimeUtil.formatInstantToIsoString(Instant.now()));
        commentResponseBody.setCommentLastUpdatedDate(DateTimeUtil.formatInstantToIsoString(Instant.now()));
        commentResponseBody.setMemberComment(true);
        commentResponseBody.setUserId(1L);
        commentResponseBody.setPostId(1L);


        when(authenticationFacade.hasAnyRole("ROLE_SUBSCRIBER")).thenReturn(true);
        when(postRepository.findByIsLiveTrueAndIsPostDeletedFalse(any(Pageable.class))).thenReturn(page);
        when(commentRepository.findTop2ByPost_PostIdOrderByCommentCreatedDateDesc(anyLong())).thenReturn(List.of(comment));
        when(modelMapper.map(any(), any())).thenReturn(commentResponseBody);

        PageableResponse<PostResponseBody> response = postService.getAllPost(0, 10, "postCreatedDate", "desc");

        assertNotNull(response);
        assertEquals(1, response.getContent().size());

    }

    @Test
    void test_getAllPost_WhenRoleIsNotSUBSCRIBER_ShouldThrowUnAuthorizedException() {
        Page<Post> page = new PageImpl<>(Collections.singletonList(testPost));

        when(postRepository.findPublicPosts(any(Pageable.class))).thenReturn(page);
        when(authenticationFacade.hasAnyRole("ROLE_SUBSCRIBER")).thenReturn(false);

        PageableResponse<PostResponseBody> response = postService.getAllPost(0, 10, "postCreatedDate", "desc");

        assertNotNull(response);
        assertEquals(1, response.getContent().size());

    }

    @Test
    void test_updatePost_WhenValidInput_ReturnUpdatedPostResponseBody() throws IOException {
        when(postRepository.findActiveAndUnarchivedPostsById(anyLong())).thenReturn(Optional.of(testPost));
        when(authenticationFacade.hasAnyRole("ROLE_ADMIN", "ROLE_MODERATOR")).thenReturn(true);
        when(postRepository.save(any(Post.class))).thenReturn(testPost);
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.ofNullable(testCategory));
        when(tagRepository.findByTagName(anyString())).thenReturn(testTags.stream().findFirst());

        when(authUtil.getAuthenticatedUser()).thenReturn(authenticatedUserDTO);
        PostRequestBody postRequestBody = new PostRequestBody();
        postRequestBody.setTitle("Updated Title");
        postRequestBody.setCategoryId(1L);
        postRequestBody.setTagNames(Collections.singleton("Java"));

        PostResponseBody response = postService.updatePost(1L, postRequestBody, image, video);

        assertNotNull(response);
        assertEquals("Updated Title", response.getTitle());
        verify(postRepository, times(1)).save(any(Post.class));

    }

    @Test
    void test_updatePost_WhenNotAuthorized_ShouldThrowUnAuthorizedException() {
        when(postRepository.findActiveAndUnarchivedPostsById(anyLong())).thenReturn(Optional.of(testPost));

        when(authUtil.getAuthenticatedUser()).thenReturn(null);

        PostRequestBody postRequestBody = new PostRequestBody();
        postRequestBody.setTitle("Updated Title");

        assertThrows(UnAuthorizedException.class, () -> postService.updatePost(1L, postRequestBody, image, video));

    }

    @Test
    void test_updatePost_WhenPostsNotExists_ShouldThrowResourceNotFoundException() {
//        when(authUtil.getAuthenticatedUser()).thenReturn(authenticatedUserDTO);

        when(postRepository.findActiveAndUnarchivedPostsById(anyLong())).thenReturn(Optional.empty());

        PostRequestBody postRequestBody = new PostRequestBody();
        postRequestBody.setTitle("Updated Title");

        assertThrows(ResourceNotFoundException.class, () -> postService.updatePost(1L, postRequestBody, image, video));

    }

    @Test
    void test_patchPost_WhenValidInput_ReturnUpdatedPostResponseBody() throws IOException {
        when(postRepository.findActiveAndUnarchivedPostsById(anyLong())).thenReturn(Optional.of(testPost));
        when(authenticationFacade.hasAnyRole("ROLE_ADMIN", "ROLE_MODERATOR")).thenReturn(true);
        when(postRepository.save(any(Post.class))).thenReturn(testPost);
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.ofNullable(testCategory));
        when(tagRepository.findByTagName(anyString())).thenReturn(testTags.stream().findFirst());

        when(authUtil.getAuthenticatedUser()).thenReturn(authenticatedUserDTO);
        PostRequestBody postRequestBody = new PostRequestBody();
        postRequestBody.setTitle("Updated Title");
        postRequestBody.setCategoryId(1L);
        postRequestBody.setTagNames(Collections.singleton("Java"));

        PostResponseBody response = postService.patchPost(1L, postRequestBody, image, video);

        assertNotNull(response);
        assertEquals("Updated Title", response.getTitle());
        verify(postRepository, times(1)).save(any(Post.class));

    }

    @Test
    void test_patchPost_WhenNotAuthorized_ShouldThrowUnAuthorizedException() {
        when(postRepository.findActiveAndUnarchivedPostsById(anyLong())).thenReturn(Optional.of(testPost));

        when(authUtil.getAuthenticatedUser()).thenReturn(null);

        PostRequestBody postRequestBody = new PostRequestBody();
        postRequestBody.setTitle("Updated Title");

        assertThrows(UnAuthorizedException.class, () -> postService.patchPost(1L, postRequestBody, image, video));

    }

    @Test
    void test_patchPost_WhenPostsNotExists_ShouldThrowResourceNotFoundException() {
//        when(authUtil.getAuthenticatedUser()).thenReturn(authenticatedUserDTO);

        when(postRepository.findActiveAndUnarchivedPostsById(anyLong())).thenReturn(Optional.empty());

        PostRequestBody postRequestBody = new PostRequestBody();
        postRequestBody.setTitle("Updated Title");

        assertThrows(ResourceNotFoundException.class, () -> postService.patchPost(1L, postRequestBody, image, video));

    }

    @Test
    void test_archivePost_WhenValidPost_ReturnArchivedPostResponseBody() {
        when(postRepository.findActiveAndUnarchivedPostsById(anyLong())).thenReturn(Optional.of(testPost));
        when(authenticationFacade.hasAnyRole("ROLE_ADMIN", "ROLE_MODERATOR")).thenReturn(true);
        when(postRepository.save(any(Post.class))).thenReturn(testPost);

        when(authUtil.getAuthenticatedUser()).thenReturn(authenticatedUserDTO);
        PostResponseBody response = postService.archivePost(1L);

        assertNotNull(response);
        assertTrue(testPost.isArchived());
        verify(postRepository, times(1)).save(any(Post.class));

    }

    @Test
    void test_unarchivePost_WhenValidPost_ReturnUnarchivedPostResponseBody() {
        testPost.setArchived(true);

        when(postRepository.findById(anyLong())).thenReturn(Optional.of(testPost));
        when(authenticationFacade.hasAnyRole("ROLE_ADMIN", "ROLE_MODERATOR")).thenReturn(true);
        when(postRepository.save(any(Post.class))).thenReturn(testPost);

        when(authUtil.getAuthenticatedUser()).thenReturn(authenticatedUserDTO);
        PostResponseBody response = postService.unarchivePost(1L);

        assertNotNull(response);
        assertFalse(testPost.isArchived());
        verify(postRepository, times(1)).save(any(Post.class));

    }

    @Test
    void test_unarchivePost_WhenPostNotExists_ShouldThrowResourceNotFoundException() {
        testPost.setArchived(true);

        when(postRepository.findById(anyLong())).thenReturn(Optional.empty());

//        when(authUtil.getAuthenticatedUser()).thenReturn(authenticatedUserDTO);

        assertThrows(ResourceNotFoundException.class, () -> postService.unarchivePost(1L));

        verify(postRepository, never()).save(any(Post.class));

    }

    @Test
    void test_unarchivePost_WhenPostIsNotArchived_ShouldThrowResourceNotFoundException() {
        testPost.setArchived(false);

        when(postRepository.findById(anyLong())).thenReturn(Optional.of(testPost));

//        when(authUtil.getAuthenticatedUser()).thenReturn(authenticatedUserDTO);

        assertThrows(InvalidPostStateException.class, () -> postService.unarchivePost(1L));

        verify(postRepository, never()).save(any(Post.class));

    }

    @Test
    void test_getArchivedPosts_WhenAuthorized_ReturnPageableResponse() {
        Page<Post> page = new PageImpl<>(Collections.singletonList(testPost));
        when(authenticationFacade.hasAnyRole("ROLE_ADMIN", "ROLE_MODERATOR")).thenReturn(true);
        when(postRepository.findActiveAndArchivedPosts(any(Pageable.class))).thenReturn(page);

        when(authUtil.getAuthenticatedUser()).thenReturn(authenticatedUserDTO);

        PageableResponse<PostResponseBody> response = postService.getArchivedPosts(0, 10, "postCreatedDate", "desc");

        assertNotNull(response);
        assertEquals(1, response.getContent().size());

    }

    @Test
    void test_getArchivedPosts_WhenNotAuthorized_ShouldThrowUnAuthorizedException() {

        when(authUtil.getAuthenticatedUser()).thenReturn(null);

        assertThrows(UnAuthorizedException.class, () -> postService.getArchivedPosts(0, 10, "postCreatedDate", "desc"));

        verify(postRepository, never()).delete(any(Post.class));

    }

    @Test
    void test_togglePostVisibility_WhenValidInput_ReturnUpdatedVisibility() {
        when(postRepository.findById(anyLong())).thenReturn(Optional.of(testPost));
        when(authenticationFacade.hasAnyRole("ROLE_ADMIN", "ROLE_MODERATOR")).thenReturn(true);
        when(postRepository.save(any(Post.class))).thenReturn(testPost);

        when(authUtil.getAuthenticatedUser()).thenReturn(authenticatedUserDTO);
        PostResponseBody response = postService.togglePostVisibility(1L, false);

        assertNotNull(response);
        assertFalse(testPost.isLive());
        verify(postRepository, times(1)).save(any(Post.class));

    }

    @Test
    void test_togglePostVisibility_WhenNotAuthorized_ShouldThrowUnAuthorizedException() {
//        when(postRepository.findById(anyLong())).thenReturn(Optional.of(testPost));

        when(authUtil.getAuthenticatedUser()).thenReturn(null);

        assertThrows(UnAuthorizedException.class, () -> postService.togglePostVisibility(1L, false));

        verify(postRepository, never()).save(any(Post.class));

    }

    @Test
    void test_setAsMemberPost_WhenUserOwnsPost_ShouldMarkAsMemberPost() {
        when(postRepository.findActiveAndUnarchivedPostsById(anyLong())).thenReturn(Optional.of(testPost));

        when(authUtil.getAuthenticatedUser()).thenReturn(authenticatedUserDTO);

        when(postRepository.save(any(Post.class))).thenReturn(testPost);

        PostResponseBody response = postService.toggleMemberPostVisibility(1L, true);

        assertNotNull(response);
        assertTrue(testPost.isMemberPost());
        verify(postRepository, times(1)).save(any(Post.class));

    }

    @Test
    void test_setAsMemberPost_WhenUserDoesNotOwnPost_ShouldThrowUnAuthorizedException() {
        User anotherUser = new User();
        anotherUser.setUserId(2L);
        anotherUser.setEmail("anotherUser@github.com");

        testPost.setUser(anotherUser);

        when(postRepository.findActiveAndUnarchivedPostsById(anyLong())).thenReturn(Optional.of(testPost));

        when(authUtil.getAuthenticatedUser()).thenReturn(authenticatedUserDTO);

        assertThrows(UnAuthorizedException.class, () -> postService.toggleMemberPostVisibility(1L, true));

        verify(postRepository, never()).save(any(Post.class));

    }

    @Test
    void test_setAsMemberPost_WhenPostDoesNotExist_ShouldThrowResourceNotFoundException() {
        when(postRepository.findActiveAndUnarchivedPostsById(anyLong())).thenReturn(Optional.empty());

        when(authUtil.getAuthenticatedUser()).thenReturn(authenticatedUserDTO);

        assertThrows(ResourceNotFoundException.class, () -> postService.toggleMemberPostVisibility(1L, true));

        verify(postRepository, never()).save(any(Post.class));

    }


    @Test
    void test_deletePost_WhenAuthorizedAndPostExists_ReturnPostDeleted() {
        when(postRepository.findById(anyLong())).thenReturn(Optional.of(testPost));
        when(authenticationFacade.hasAnyRole("ROLE_ADMIN", "ROLE_MODERATOR")).thenReturn(true);

        when(authUtil.getAuthenticatedUser()).thenReturn(authenticatedUserDTO);
        postService.deletePost(1L);

        verify(postRepository, times(1)).delete(any(Post.class));

    }

    @Test
    void test_deletePost_WhenAuthorizedAndPostDoesNotExists_ShouldThrowResourceNotFoundException() {
        when(postRepository.findById(anyLong())).thenReturn(Optional.empty());

        when(authUtil.getAuthenticatedUser()).thenReturn(authenticatedUserDTO);

        assertThrows(ResourceNotFoundException.class, () -> postService.deletePost(1L));

        verify(postRepository, never()).delete(any(Post.class));

    }

    @Test
    void test_deletePost_WhenNotAuthorized_ShouldThrowUnAuthorizedException() {

        when(authUtil.getAuthenticatedUser()).thenReturn(null);

        assertThrows(UnAuthorizedException.class, () -> postService.deletePost(1L));

        verify(postRepository, never()).delete(any(Post.class));

    }

    @Test
    void test_deletePost_WhenPostAlreadyDeleted_ShouldThrowResourceNotFoundException() {
        testPost.setPostDeleted(true);

        when(postRepository.findById(anyLong())).thenReturn(Optional.of(testPost));
        when(authenticationFacade.hasAnyRole("ROLE_ADMIN", "ROLE_MODERATOR")).thenReturn(true);

        when(authUtil.getAuthenticatedUser()).thenReturn(authenticatedUserDTO);

        assertThrows(ResourceNotFoundException.class, () -> postService.deletePost(1L));

        verify(postRepository, never()).delete(any(Post.class));

    }

    @Test
    void test_getPostsByUser_WhenUserExists_ReturnPageableResponse() {

        when(userRepository.findByUserName(anyString())).thenReturn(Optional.of(testUser));
        Page<Post> page = new PageImpl<>(Collections.singletonList(testPost));
        when(postRepository.findByUser(any(User.class), any(Pageable.class))).thenReturn(page);


        PageableResponse<PostResponseBody> response = postService.getPostsByUser("testUser", 0, 10, "postCreatedDate", "desc");

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        verify(userRepository, times(1)).findByUserName(anyString());
        verify(postRepository, times(1)).findByUser(any(User.class), any(Pageable.class));
    }

    @Test
    void test_getPostsByUser_WhenUserNotFound_ShouldThrowResourceNotFoundException() {

        when(userRepository.findByUserName(anyString())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> postService.getPostsByUser("nonexistentUser", 0, 10, "postCreatedDate", "desc"));

        verify(userRepository, times(1)).findByUserName(anyString());
        verify(postRepository, never()).findByUser(any(User.class), any(Pageable.class));
    }

    @Test
    void test_getPostsByCategory_WhenCategoryExists_ReturnPageableResponse() {

        when(categoryRepository.findById(anyLong())).thenReturn(Optional.of(testCategory));

        Page<Post> page = new PageImpl<>(Collections.singletonList(testPost));
        when(postRepository.findByCategory(any(Category.class), any(Pageable.class))).thenReturn(page);


        PageableResponse<PostResponseBody> response = postService.getPostsByCategory(1L, 0, 10, "postCreatedDate", "desc");

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        verify(categoryRepository, times(1)).findById(anyLong());
        verify(postRepository, times(1)).findByCategory(any(Category.class), any(Pageable.class));
    }

    @Test
    void test_getPostsByCategory_WhenCategoryNotFound_ShouldThrowResourceNotFoundException() {

        when(categoryRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> postService.getPostsByCategory(99L, 0, 10, "postCreatedDate", "desc"));

        verify(categoryRepository, times(1)).findById(anyLong());
        verify(postRepository, never()).findByCategory(any(Category.class), any(Pageable.class));
    }

    @Test
    void test_searchPostsByTitle_WhenKeywordMatches_ReturnPageableResponse() {

        Page<Post> page = new PageImpl<>(Collections.singletonList(testPost));
        when(postRepository.searchKeyOnTitle(anyString(), any(Pageable.class))).thenReturn(page);


        PageableResponse<PostResponseBody> response = postService.searchPostsByTitle("Test", 0, 10, "postCreatedDate", "desc");

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        verify(postRepository, times(1)).searchKeyOnTitle(anyString(), any(Pageable.class));
    }

    @Test
    void test_searchPostsByTitle_WhenNoMatches_ReturnEmptyPageableResponse() {
        Page<Post> page = new PageImpl<>(Collections.emptyList());
        when(postRepository.searchKeyOnTitle(anyString(), any(Pageable.class))).thenReturn(page);

        PageableResponse<PostResponseBody> response = postService.searchPostsByTitle("Unknown", 0, 10, "postCreatedDate", "desc");

        assertNotNull(response);
        assertTrue(response.getContent().isEmpty());
        verify(postRepository, times(1)).searchKeyOnTitle(anyString(), any(Pageable.class));
    }

    @Test
    void test_deactivatePost_WhenUserAuthorized_ShouldDeactivatePost() {

        Comment comment = new Comment();
        comment.setCommentId(1L);
        comment.setCommentDesc("Awesome Post");
        comment.setCommentDeleted(false);
        comment.setCommentLastUpdatedDate(Instant.now());
        testPost.setComments(List.of(comment));

        when(postRepository.findById(anyLong())).thenReturn(Optional.of(testPost));
        when(authenticationFacade.hasAnyRole("ROLE_ADMIN", "ROLE_MODERATOR")).thenReturn(true);
        when(postRepository.save(any(Post.class))).thenReturn(testPost);

        when(authUtil.getAuthenticatedUser()).thenReturn(authenticatedUserDTO);

        PostResponseBody response = postService.deactivatePost(1L);

        assertNotNull(response);
        assertTrue(testPost.isPostDeleted());
        verify(postRepository, times(1)).save(any(Post.class));

    }

    @Test
    void test_deactivatePost_WhenUserNotAuthorized_ShouldThrowUnAuthorizedException() {

        when(postRepository.findById(anyLong())).thenReturn(Optional.of(testPost));
        when(authUtil.getAuthenticatedUser()).thenReturn(null);

        assertThrows(UnAuthorizedException.class, () -> postService.deactivatePost(1L));

        verify(postRepository, never()).save(any(Post.class));

    }

    @Test
    void test_activatePost_WhenPostCanBeActivated_ShouldActivatePost() {

        testPost.setPostDeleted(true);
        testPost.setPostDeletionTimestamp(Instant.now().minus(30, ChronoUnit.DAYS)); // Within the allowed range
        Comment comment = new Comment();
        comment.setCommentId(1L);
        comment.setCommentDesc("Awesome Post");
        comment.setCommentDeleted(true);
        comment.setCommentLastUpdatedDate(Instant.now());
        comment.setCommentDeletionTimestamp(Instant.now());
        testPost.setComments(List.of(comment));

        when(postRepository.findById(anyLong())).thenReturn(Optional.of(testPost));
        when(authenticationFacade.hasAnyRole("ROLE_ADMIN", "ROLE_MODERATOR")).thenReturn(true);
        when(postRepository.save(any(Post.class))).thenReturn(testPost);

        when(authUtil.getAuthenticatedUser()).thenReturn(authenticatedUserDTO);

        PostResponseBody response = postService.activatePost(1L);

        assertNotNull(response);
        assertFalse(testPost.isPostDeleted());
        verify(postRepository, times(1)).save(any(Post.class));

    }

    @Test
    void test_activatePost_WhenPostCannotBeActivated_ShouldNotChangePostStatus() {

        testPost.setPostDeleted(true);
        testPost.setPostDeletionTimestamp(Instant.now().minus(120, ChronoUnit.DAYS)); // Beyond the allowed range

        when(postRepository.findById(anyLong())).thenReturn(Optional.of(testPost));

        when(authUtil.getAuthenticatedUser()).thenReturn(authenticatedUserDTO);

        PostResponseBody response = postService.activatePost(2L);

        assertNotNull(response);
        assertTrue(testPost.isPostDeleted()); // Should still be deleted

        verify(postRepository, times(1)).findById(anyLong());
        verify(postRepository, never()).save(any(Post.class)); // Ensure post is NOT saved

    }
}