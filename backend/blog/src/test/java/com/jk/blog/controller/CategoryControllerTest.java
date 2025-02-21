package com.jk.blog.controller;

import com.jk.blog.dto.APIResponse;
import com.jk.blog.dto.CategoryDTO;
import com.jk.blog.service.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryControllerTest {

    @InjectMocks
    private CategoryController categoryController;

    @Mock
    private CategoryService categoryService;

    private CategoryDTO categoryDTO;

    @BeforeEach
    void setUp() {
        categoryDTO = new CategoryDTO();
        categoryDTO.setCategoryId(1L);
        categoryDTO.setCategoryTitle("Technology");
        categoryDTO.setCategoryDescription("All about technology");
    }

    @Test
    void test_createCategory_whenValidRequest_returnCreatedCategory() {
        when(categoryService.createCategory(any(CategoryDTO.class))).thenReturn(categoryDTO);

        ResponseEntity<APIResponse<CategoryDTO>> response = categoryController.createCategory(categoryDTO);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertTrue(response.getBody().getSuccess());
        assertEquals("Technology", response.getBody().getData().getCategoryTitle());

        verify(categoryService, times(1)).createCategory(any(CategoryDTO.class));
    }

    @Test
    void test_getCategoryById_whenValidId_returnCategory() {
        when(categoryService.getCategoryById(1L)).thenReturn(categoryDTO);

        ResponseEntity<APIResponse<CategoryDTO>> response = categoryController.getCategoryById(1L);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().getSuccess());
        assertEquals(1L, response.getBody().getData().getCategoryId());

        verify(categoryService, times(1)).getCategoryById(1L);
    }

    @Test
    void test_getCategoryByTitle_whenValidTitle_returnCategory() {
        when(categoryService.getCategoryByTitle("Technology")).thenReturn(categoryDTO);

        ResponseEntity<APIResponse<CategoryDTO>> response = categoryController.getCategoryByTitle("Technology");

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().getSuccess());
        assertEquals("Technology", response.getBody().getData().getCategoryTitle());

        verify(categoryService, times(1)).getCategoryByTitle("Technology");
    }

    @Test
    void test_getAllCategory_whenCategoriesExist_returnCategoryList() {
        List<CategoryDTO> categoryDTOList = Arrays.asList(categoryDTO, new CategoryDTO());
        when(categoryService.getAllCategories()).thenReturn(categoryDTOList);

        ResponseEntity<APIResponse<List<CategoryDTO>>> response = categoryController.getAllCategory();

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().getSuccess());
        assertEquals(2, response.getBody().getData().size());

        verify(categoryService, times(1)).getAllCategories();
    }

    @Test
    void test_updateCategory_whenValidRequest_returnUpdatedCategory() {
        when(categoryService.updateCategory(any(CategoryDTO.class), anyLong())).thenReturn(categoryDTO);

        ResponseEntity<APIResponse<CategoryDTO>> response = categoryController.updateCategory(categoryDTO, 1L);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().getSuccess());
        assertEquals("Technology", response.getBody().getData().getCategoryTitle());

        verify(categoryService, times(1)).updateCategory(any(CategoryDTO.class), eq(1L));
    }

    @Test
    void test_deleteCategory_whenValidId_returnSuccessMessage() {
        doNothing().when(categoryService).deleteCategory(1L);

        ResponseEntity<APIResponse<Void>> response = categoryController.deleteCategory(1L);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().getSuccess());

        verify(categoryService, times(1)).deleteCategory(1L);
    }
}