package com.arthur.digitalcommerce.service;

import com.arthur.digitalcommerce.exceptions.APIException;
import com.arthur.digitalcommerce.exceptions.ResourceNotFoundException;
import com.arthur.digitalcommerce.model.Category;
import com.arthur.digitalcommerce.payload.CategoryDTO;
import com.arthur.digitalcommerce.payload.CategoryResponse;
import com.arthur.digitalcommerce.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private Category category;
    private CategoryDTO categoryDTO;
    private Long categoryId = 1L;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setCategoryId(categoryId);
        category.setCategoryName("Electronics");

        categoryDTO = new CategoryDTO();
        categoryDTO.setCategoryId(categoryId);
        categoryDTO.setCategoryName("Electronics");
    }

    @Test
    void getAllCategories_shouldReturnCategoryResponse_whenCategoriesExist() {
        Page<Category> categoryPage = new PageImpl<>(Collections.singletonList(category));
        when(categoryRepository.findAll(any(Pageable.class))).thenReturn(categoryPage);
        when(modelMapper.map(any(Category.class), eq(CategoryDTO.class))).thenReturn(categoryDTO);

        CategoryResponse result = categoryService.getAllCategories(0, 5, "categoryId", "asc");

        assertNotNull(result);
        assertFalse(result.getContent().isEmpty());
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getAllCategories_shouldThrowException_whenNoCategoriesExist() {
        Page<Category> emptyPage = new PageImpl<>(Collections.emptyList());
        when(categoryRepository.findAll(any(Pageable.class))).thenReturn(emptyPage);

        assertThrows(APIException.class, () -> categoryService.getAllCategories(0, 5, "categoryId", "asc"));
    }

    @Test
    void createCategory_shouldReturnSavedCategoryDTO_whenCategoryNameIsUnique() {
        when(modelMapper.map(any(CategoryDTO.class), eq(Category.class))).thenReturn(category);
        when(categoryRepository.findByCategoryName(anyString())).thenReturn(null);
        when(categoryRepository.save(any(Category.class))).thenReturn(category);
        when(modelMapper.map(any(Category.class), eq(CategoryDTO.class))).thenReturn(categoryDTO);

        CategoryDTO result = categoryService.createCategory(categoryDTO);

        assertNotNull(result);
        assertEquals(categoryDTO.getCategoryName(), result.getCategoryName());
        verify(categoryRepository, times(1)).save(category);
    }

    @Test
    void createCategory_shouldThrowException_whenCategoryNameAlreadyExists() {
        when(modelMapper.map(any(CategoryDTO.class), eq(Category.class))).thenReturn(category);
        when(categoryRepository.findByCategoryName(anyString())).thenReturn(category);

        assertThrows(APIException.class, () -> categoryService.createCategory(categoryDTO));
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void deleteCategory_shouldSucceed_whenCategoryExists() {
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        doNothing().when(categoryRepository).delete(any(Category.class));
        when(modelMapper.map(any(Category.class), eq(CategoryDTO.class))).thenReturn(categoryDTO);

        CategoryDTO result = categoryService.deleteCategory(categoryId);

        assertNotNull(result);
        verify(categoryRepository, times(1)).delete(category);
    }

    @Test
    void deleteCategory_shouldThrowException_whenCategoryNotFound() {
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> categoryService.deleteCategory(categoryId));
        verify(categoryRepository, never()).delete(any(Category.class));
    }

    @Test
    void updateCategory_shouldReturnUpdatedCategoryDTO_whenCategoryExists() {
        CategoryDTO updateDTO = new CategoryDTO();
        updateDTO.setCategoryName("New Electronics");

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        lenient().when(modelMapper.map(any(Category.class), eq(CategoryDTO.class))).thenReturn(updateDTO);

        categoryService.updateCategory(updateDTO, categoryId);

        ArgumentCaptor<Category> categoryCaptor = ArgumentCaptor.forClass(Category.class);
        verify(categoryRepository, times(1)).save(categoryCaptor.capture());
        Category capturedCategory = categoryCaptor.getValue();

        assertEquals(categoryId, capturedCategory.getCategoryId());
    }

    @Test
    void updateCategory_shouldThrowException_whenCategoryNotFound() {
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> categoryService.updateCategory(categoryDTO, categoryId));
        verify(categoryRepository, never()).save(any(Category.class));
    }
}