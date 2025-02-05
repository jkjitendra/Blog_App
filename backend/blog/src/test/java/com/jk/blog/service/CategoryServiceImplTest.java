package com.jk.blog.service;

import com.jk.blog.dto.CategoryDTO;
import com.jk.blog.entity.Category;
import com.jk.blog.exception.ResourceAlreadyExistsException;
import com.jk.blog.exception.ResourceNotFoundException;
import com.jk.blog.repository.CategoryRepository;
import com.jk.blog.service.impl.CategoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private Category category;
    private CategoryDTO categoryDTO;

    @BeforeEach
    void setup() {
        category = new Category();
        category.setCategoryId(1L);
        category.setCategoryTitle("Technology");
        category.setCategoryDescription("All about technology");

        categoryDTO = new CategoryDTO();
        categoryDTO.setCategoryId(1L);
        categoryDTO.setCategoryTitle("Technology");
        categoryDTO.setCategoryDescription("All about technology");
    }

    @Test
    void test_createCategory_ShouldReturnCategoryDTO_WhenValidInput() {

        when(modelMapper.map(categoryDTO, Category.class)).thenReturn(category);
        when(categoryRepository.findByCategoryTitle(categoryDTO.getCategoryTitle())).thenReturn(Optional.empty());
        when(categoryRepository.save(category)).thenReturn(category);
        when(modelMapper.map(category, CategoryDTO.class)).thenReturn(categoryDTO);

        CategoryDTO result = categoryService.createCategory(categoryDTO);

        assertNotNull(result);
        assertEquals("Technology", result.getCategoryTitle());
        verify(categoryRepository, times(1)).save(category);

    }

    @Test
    void test_createCategory_ShouldThrowResourceAlreadyExistsException_WhenCategoryTitleAlreadyExists() {

        when(modelMapper.map(categoryDTO, Category.class)).thenReturn(category);
        when(categoryRepository.findByCategoryTitle(categoryDTO.getCategoryTitle())).thenReturn(Optional.of(category));

        assertThrows(ResourceAlreadyExistsException.class, () -> categoryService.createCategory(categoryDTO));
        verify(categoryRepository, never()).save(any(Category.class));

    }

    @Test
    void test_getCategoryById_ShouldReturnCategoryDTO_WhenCategoryIdExists() {

        when(categoryRepository.findById(categoryDTO.getCategoryId())).thenReturn(Optional.of(category));
        when(modelMapper.map(category, CategoryDTO.class)).thenReturn(categoryDTO);

        CategoryDTO result = categoryService.getCategoryById(categoryDTO.getCategoryId());

        assertNotNull(result);
        verify(categoryRepository, times(1)).findById(any());

    }

    @Test
    void test_getCategoryById_ShouldThrowResourceNotFoundException_WhenCategoryIdNotExists() {

        when(categoryRepository.findById(categoryDTO.getCategoryId())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> categoryService.getCategoryById(category.getCategoryId()));

    }

    @Test
    void test_getCategoryByTitle_ShouldReturnCategoryDTO_WhenCategoryTitleExists() {

        when(categoryRepository.findByCategoryTitle(any())).thenReturn(Optional.of(category));
        when(modelMapper.map(category, CategoryDTO.class)).thenReturn(categoryDTO);

        CategoryDTO result = categoryService.getCategoryByTitle(categoryDTO.getCategoryTitle());

        assertNotNull(result);
        verify(categoryRepository, times(1)).findByCategoryTitle(any());

    }

    @Test
    void test_getCategoryByTitle_ShouldThrowResourceNotFoundException_WhenCategoryTitleNotExists() {

        when(categoryRepository.findByCategoryTitle(any())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> categoryService.getCategoryByTitle(category.getCategoryTitle()));

    }

    @Test
    void test_getAllCategory_ShouldReturnListOfCategoryDTO_WhenCategoryExists() {

        when(categoryRepository.findAll()).thenReturn(List.of(category));
        when(modelMapper.map(category, CategoryDTO.class)).thenReturn(categoryDTO);

        List<CategoryDTO> result = categoryService.getAllCategories();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(categoryRepository, times(1)).findAll();

    }

    @Test
    void test_updateCategory_ShouldReturnCategoryDTO_WhenCategoryExists() {
        CategoryDTO updatedCategoryDTO = new CategoryDTO();
        updatedCategoryDTO.setCategoryTitle("Updated Tech");
        updatedCategoryDTO.setCategoryDescription("Updated Description");

        when(categoryRepository.findById(any())).thenReturn(Optional.ofNullable(category));
        when(categoryRepository.save(any(Category.class))).thenReturn(category);
        when(modelMapper.map(category, CategoryDTO.class)).thenReturn(updatedCategoryDTO);

        CategoryDTO result = categoryService.updateCategory(updatedCategoryDTO, 1L);

        assertNotNull(result);
        assertEquals("Updated Tech", result.getCategoryTitle());
        verify(categoryRepository, times(1)).save(any(Category.class));

    }

    @Test
    void test_updateCategory_ShouldThrowResourceNotFound_WhenCategoryNotExists() {
        CategoryDTO updatedCategoryDTO = new CategoryDTO();
        updatedCategoryDTO.setCategoryTitle("Updated Tech");
        updatedCategoryDTO.setCategoryDescription("Updated Description");

        when(categoryRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> categoryService.updateCategory(updatedCategoryDTO, 1L));
        verify(categoryRepository, never()).save(any(Category.class));

    }

    @Test
    void test_deleteCategory_ShouldDeleteCategory_WhenCategoryExists() {

        when(categoryRepository.findById(any())).thenReturn(Optional.ofNullable(category));
        doNothing().when(categoryRepository).delete(any(Category.class));

        categoryService.deleteCategory(category.getCategoryId());

        verify(categoryRepository, times(1)).delete(any(Category.class));

    }

    @Test
    void test_deleteCategory_ShouldThrowResourceNotFound_WhenCategoryNotExists() {

        when(categoryRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> categoryService.deleteCategory(category.getCategoryId()));
        verify(categoryRepository, never()).delete(any(Category.class));

    }


}
