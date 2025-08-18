package com.arthur.digitalcommerce.controller;

import com.arthur.digitalcommerce.config.AppConstants;
import com.arthur.digitalcommerce.payload.CategoryDTO;
import com.arthur.digitalcommerce.payload.CategoryResponse;
import com.arthur.digitalcommerce.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping("/public/categories/paged")
    public ResponseEntity<CategoryResponse> getAllCategoriesPaged(
            @RequestParam(name = "page",      defaultValue = AppConstants.PAGE_NUMBER) Integer page,
            @RequestParam(name = "size",      defaultValue = AppConstants.PAGE_SIZE)   Integer size,
            @RequestParam(name = "sortBy",    defaultValue = AppConstants.SORT_CATEGORIES_BY) String sortBy,
            @RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_DIR)           String sortOrder) {

        CategoryResponse categoryResponse =
                categoryService.getAllCategories(page, size, sortBy, sortOrder);
        return ResponseEntity.ok(categoryResponse);
    }


    @PostMapping("/public/categories")
    public ResponseEntity<CategoryDTO> createCategory(@Valid @RequestBody CategoryDTO categoryDTO){
        CategoryDTO savedCategoryDTO = categoryService.createCategory(categoryDTO);
        return new ResponseEntity<>(savedCategoryDTO, HttpStatus.CREATED);
    }

    @DeleteMapping("/admin/categories/{categoryId}")
    public ResponseEntity<CategoryDTO> deleteCategory(@PathVariable Long categoryId){
        CategoryDTO deletedCategory = categoryService.deleteCategory(categoryId);
        return new ResponseEntity<>(deletedCategory, HttpStatus.OK);
    }


    @PutMapping("/public/categories/{categoryId}")
    public ResponseEntity<CategoryDTO> updateCategory(@Valid @RequestBody CategoryDTO categoryDTO,
                                                      @PathVariable Long categoryId){
        CategoryDTO savedCategoryDTO = categoryService.updateCategory(categoryDTO, categoryId);
        return new ResponseEntity<>(savedCategoryDTO, HttpStatus.OK);
    }
}
