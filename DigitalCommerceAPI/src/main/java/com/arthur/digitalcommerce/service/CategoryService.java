package com.arthur.digitalcommerce.service;

import com.arthur.digitalcommerce.payload.CategoryDTO;
import com.arthur.digitalcommerce.payload.CategoryResponse;

public interface CategoryService {
    CategoryResponse getAllCategories(int page, int size, String sortBy, String sortDir);
    CategoryDTO getCategoryById(Long id);
    CategoryDTO addCategory(CategoryDTO categoryDTO);
    CategoryDTO updateCategory(Long id, CategoryDTO categoryDTO);
    void deleteCategoryById(Long id);
}
