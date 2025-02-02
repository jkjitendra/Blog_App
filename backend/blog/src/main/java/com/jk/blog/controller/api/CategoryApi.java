package com.jk.blog.controller.api;

import com.jk.blog.dto.APIResponse;
import com.jk.blog.dto.CategoryDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public interface CategoryApi {

    @Operation(summary = "Create a category", description = "Allows admins to create a new category.")
    @ApiResponse(responseCode = "201", description = "Category created successfully",
            content = @Content(schema = @Schema(implementation = APIResponse.class)))
    @ApiResponse(responseCode = "400", description = "Invalid request data")
    @ApiResponse(responseCode = "403", description = "Forbidden - User lacks necessary permissions")
    ResponseEntity<APIResponse<CategoryDTO>> createCategory(@Valid @RequestBody CategoryDTO categoryDTO);

    @Operation(summary = "Get all categories", description = "Fetches all categories.")
    @ApiResponse(responseCode = "200", description = "Categories fetched successfully",
            content = @Content(schema = @Schema(implementation = APIResponse.class)))
    @ApiResponse(responseCode = "500", description = "Internal server error")
    ResponseEntity<APIResponse<List<CategoryDTO>>> getAllCategory();

    @Operation(summary = "Get category by ID", description = "Fetches a category by its ID.")
    @ApiResponse(responseCode = "200", description = "Category fetched successfully",
            content = @Content(schema = @Schema(implementation = APIResponse.class)))
    @ApiResponse(responseCode = "404", description = "Category not found")
    ResponseEntity<APIResponse<CategoryDTO>> getCategory(@PathVariable Long categoryId);

    @Operation(summary = "Update a category", description = "Allows admins to update a category by its ID.")
    @ApiResponse(responseCode = "200", description = "Category updated successfully",
            content = @Content(schema = @Schema(implementation = APIResponse.class)))
    @ApiResponse(responseCode = "400", description = "Invalid request data")
    @ApiResponse(responseCode = "403", description = "Forbidden - User lacks necessary permissions")
    @ApiResponse(responseCode = "404", description = "Category not found")
    ResponseEntity<APIResponse<CategoryDTO>> updateCategory(@Valid @RequestBody CategoryDTO categoryDTO, @PathVariable Long categoryId);

    @Operation(summary = "Delete a category", description = "Allows admins to delete a category by its ID.")
    @ApiResponse(responseCode = "200", description = "Category deleted successfully",
            content = @Content(schema = @Schema(implementation = APIResponse.class)))
    @ApiResponse(responseCode = "403", description = "Forbidden - User lacks necessary permissions")
    @ApiResponse(responseCode = "404", description = "Category not found")
    ResponseEntity<APIResponse<Void>> deleteCategory(@PathVariable Long categoryId);

}
