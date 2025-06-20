package com.arthur.digitalcommerce.service;

import com.arthur.digitalcommerce.model.Category;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CategoryService {

    private final List<Category> categories = new ArrayList<>();
    private Long nextId = 1L;

    public CategoryService() {
        try {
            categories.add(new Category(1L, "Artesanato"));
            categories.add(new Category(2L, "Decoração"));
            categories.add(new Category(3L, "Infantil"));
            categories.add(new Category(4L, "Aromas"));
        } catch (Exception e) {
            System.out.println("Erro ao inicializar categorias: " + e.getMessage());
        }
    }

    public List<Category> getAllCategories() {
        try {
            return categories;
        } catch (Exception e) {
            System.out.println("Erro ao buscar todas as categorias: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public Category getCategoryById(Long id) {
        try {
            return categories.stream()
                    .filter(c -> c.getCategoryId().equals(id))
                    .findFirst()
                    .orElse(null);
        } catch (Exception e) {
            System.out.println("Erro ao buscar categoria por ID: " + e.getMessage());
            return null;
        }
    }

    public Category addCategory(Category category) {
        try {
            category.setCategoryId(nextId++);
            categories.add(category);
            return category;
        } catch (Exception e) {
            System.out.println("Erro ao adicionar categoria: " + e.getMessage());
            return null;
        }
    }

    public boolean deleteCategoryById(Long id) {
        try {
            return categories.removeIf(c -> c.getCategoryId().equals(id));
        } catch (Exception e) {
            System.out.println("Erro ao deletar categoria: " + e.getMessage());
            return false;
        }
    }

    public Category updateCategory(Long id, Category updatedCategory) {
        try {
            for (Category category : categories) {
                if (category.getCategoryId().equals(id)) {
                    category.setCategoryName(updatedCategory.getCategoryName());
                    return category;
                }
            }
            return null;
        } catch (Exception e) {
            System.out.println("Erro ao atualizar categoria: " + e.getMessage());
            return null;
        }
    }
}
