package com.example.demo.services.categories;

import com.example.demo.dtos.CategoryDTO;
import com.example.demo.models.Category;
import com.example.demo.models.Product;
import com.example.demo.repositories.CategoryRepository;
import com.example.demo.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@Slf4j
@RequiredArgsConstructor
public class CategoryService implements ICategoryService {

	private final CategoryRepository categoryRepository;
	private final ProductRepository productRepository;

	@Override
	public Category createCategory(CategoryDTO category) {
		Category newCategory = Category.builder().name(category.getName()).build();
		return categoryRepository.save(newCategory);
	}

	@Override
	public Category getCategoryById(long id) {
		return categoryRepository.findById(id).orElseThrow(() -> new RuntimeException("Category not found"));
	}

	@Override
	public List<Category> getAllCategories() {
		return categoryRepository.findAll();
	}

	@Override
	public Category updateCategory(long categoryId, CategoryDTO category) {
		Category existingCategory = categoryRepository.findById(categoryId).orElseThrow(() -> new RuntimeException("Category not found"));
		existingCategory.setName(category.getName());
		categoryRepository.save(existingCategory);
		return existingCategory;
	}

	@Override
	public Category deleteCategory(long id) throws Exception {

		Category category = categoryRepository.findById(id).orElseThrow(() -> new ChangeSetPersister.NotFoundException());
		List<Product> products = productRepository.findByCategory(category);

		if (!products.isEmpty()){
			throw new IllegalStateException("Cannot delete category with associated products");
		}
		else{
			categoryRepository.deleteById(id);
			return category;
		}
	}
}
