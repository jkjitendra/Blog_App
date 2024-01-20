package com.jk.blog.service;

import com.jk.blog.dto.CategoryDTO;

import java.util.List;

public interface CategoryService {

    CategoryDTO createCategory(CategoryDTO categoryDTO);
    CategoryDTO updateCategory(CategoryDTO categoryDTO, Long categoryId);
    CategoryDTO getCategoryById(Long categoryId);
    List<CategoryDTO> getAllCategories();
    void deleteCategory(Long categoryId);
}
