package com.jk.blog.service.impl;

import com.jk.blog.dto.CategoryDTO;
import com.jk.blog.entity.Category;
import com.jk.blog.exception.ResourceAlreadyExistsException;
import com.jk.blog.exception.ResourceNotFoundException;
import com.jk.blog.repository.CategoryRepository;
import com.jk.blog.service.CategoryService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private ModelMapper modelMapper;

    @Override
    @Transactional
    public CategoryDTO createCategory(CategoryDTO categoryDTO) {
        Category category = this.modelMapper.map(categoryDTO, Category.class);
        Optional<Category> existingCategory = this.categoryRepository.findByCategoryTitle(categoryDTO.getCategoryTitle());
        if (existingCategory.isPresent()) {
            throw new ResourceAlreadyExistsException("Category", "title", category.getCategoryTitle());
        }
        category.setCategoryTitle(category.getCategoryTitle().toLowerCase());
        Category savedCategory = this.categoryRepository.save(category);
        return this.modelMapper.map(savedCategory, CategoryDTO.class);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryDTO getCategoryById(Long categoryId) {
        Category category = this.categoryRepository
                                .findById(categoryId)
                                .orElseThrow(() -> new ResourceNotFoundException("Category", "Category Id", categoryId));
        return this.modelMapper.map(category, CategoryDTO.class);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryDTO getCategoryByTitle(String categoryTitle) {
        Category category = this.categoryRepository
                .findByCategoryTitle(categoryTitle.toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "Category Title", categoryTitle));
        return this.modelMapper.map(category, CategoryDTO.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDTO> getAllCategories() {
        List<Category> categoryList = this.categoryRepository.findAll();

        return categoryList.stream()
                           .map((eachCategory) -> this.modelMapper.map(eachCategory, CategoryDTO.class))
                           .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CategoryDTO updateCategory(CategoryDTO categoryDTO, Long categoryId) {
        Category category = this.categoryRepository
                                .findById(categoryId)
                                .orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", categoryId));
        category.setCategoryTitle(categoryDTO.getCategoryTitle());
        category.setCategoryDescription(categoryDTO.getCategoryDescription());
        Category updatedCategory = this.categoryRepository.save(category);
        return this.modelMapper.map(updatedCategory, CategoryDTO.class);
    }

    @Override
    @Transactional
    public void deleteCategory(Long categoryId) {
        Category category = this.categoryRepository
                                .findById(categoryId)
                                .orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", categoryId));
        this.categoryRepository.delete(category);
    }
}
