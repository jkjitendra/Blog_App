package com.jk.blog.controller;

import com.jk.blog.dto.APIResponse;
import com.jk.blog.dto.CategoryDTO;
import com.jk.blog.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @PreAuthorize("hasAuthority('CATEGORY_MANAGE')")
    @PostMapping("/")
    public ResponseEntity<APIResponse<CategoryDTO>> createCategory(@Valid @RequestBody CategoryDTO categoryDTO) {
        CategoryDTO createdCategory = this.categoryService.createCategory(categoryDTO);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new APIResponse<>(true, "Category created successfully", createdCategory));
    }

    @GetMapping("/")
    public ResponseEntity<APIResponse<List<CategoryDTO>>> getAllCategory() {
        List<CategoryDTO> categoryDTOList = this.categoryService.getAllCategories();
        return ResponseEntity
                .ok(new APIResponse<>(true, "Categories fetched successfully", categoryDTOList));
    }

    @GetMapping("/{categoryId}")
    public ResponseEntity<APIResponse<CategoryDTO>> getCategory(@PathVariable Long categoryId) {
        CategoryDTO categoryDTO = this.categoryService.getCategoryById(categoryId);
        return ResponseEntity
                .ok(new APIResponse<>(true, "Category fetched successfully", categoryDTO));
    }

    @PreAuthorize("hasAuthority('CATEGORY_MANAGE')")
    @PutMapping("/{categoryId}")
    public ResponseEntity<APIResponse<CategoryDTO>> updateCategory(@Valid @RequestBody CategoryDTO categoryDTO, @PathVariable Long categoryId) {
        CategoryDTO updatedCategory = this.categoryService.updateCategory(categoryDTO, categoryId);
        return ResponseEntity
                .ok(new APIResponse<>(true, "Category updated successfully", updatedCategory));
    }

    @PreAuthorize("hasAuthority('CATEGORY_MANAGE')")
    @DeleteMapping("/{categoryId}")
    public ResponseEntity<APIResponse<Void>> deleteCategory(@PathVariable Long categoryId) {
        this.categoryService.deleteCategory(categoryId);
        return ResponseEntity
                .ok(new APIResponse<>(true, "Category deleted successfully"));
    }
}
