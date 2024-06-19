package com.example.demo.services.categories;

import com.example.demo.dtos.CategoryDTO;
import com.example.demo.models.Category;

import java.util.List;

public interface ICategoryService {
	Category createCategory(CategoryDTO category);

	Category getCategoryById(long id);

	List<Category> getAllCategories();

	Category updateCategory(long categoryId, CategoryDTO category);

	Category deleteCategory(long id) throws Exception;

}
