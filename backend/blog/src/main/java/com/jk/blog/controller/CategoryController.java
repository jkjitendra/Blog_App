package com.jk.blog.controller;

import com.jk.blog.constants.SecurityConstants;
import com.jk.blog.controller.api.CategoryApi;
import com.jk.blog.dto.APIResponse;
import com.jk.blog.dto.CategoryDTO;
import com.jk.blog.service.CategoryService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@SecurityRequirement(name = SecurityConstants.SECURITY_SCHEME_NAME)
@Tag(name = "Category Management", description = "APIs for managing blog categories")
public class CategoryController implements CategoryApi {

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

    @GetMapping("/{categoryId}")
    public ResponseEntity<APIResponse<CategoryDTO>> getCategoryById(@PathVariable Long categoryId) {
        CategoryDTO categoryDTO = this.categoryService.getCategoryById(categoryId);
        return ResponseEntity
                .ok(new APIResponse<>(true, "Category fetched successfully", categoryDTO));
    }

    @GetMapping("/{categoryTitle}")
    public ResponseEntity<APIResponse<CategoryDTO>> getCategoryByTitle(@PathVariable String categoryTitle) {
        CategoryDTO categoryDTO = this.categoryService.getCategoryByTitle(categoryTitle);
        return ResponseEntity
                .ok(new APIResponse<>(true, "Category fetched successfully", categoryDTO));
    }

    @GetMapping("/")
    public ResponseEntity<APIResponse<List<CategoryDTO>>> getAllCategory() {
        List<CategoryDTO> categoryDTOList = this.categoryService.getAllCategories();

        return ResponseEntity
                .ok(new APIResponse<>(true, "Categories fetched successfully", categoryDTOList));
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
