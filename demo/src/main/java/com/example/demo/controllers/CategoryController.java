package com.example.demo.controllers;


import com.example.demo.dtos.CategoryDTO;
import com.example.demo.models.Category;
import com.example.demo.responses.ResponseObject;
import com.example.demo.services.categories.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.FieldError;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/categories")
@RequiredArgsConstructor
public class CategoryController {
	private final CategoryService categoryService;

	@PostMapping("")
	public ResponseEntity<ResponseObject> createCategory(
			@Valid @RequestBody CategoryDTO categoryDTO,
			BindingResult result) {

		if (result.hasErrors()) {
			List<String> errorMessages = result.getFieldErrors()
					.stream()
					.map(FieldError::getDefaultMessage)
					.toList();
			return ResponseEntity.ok().body(ResponseObject.builder()
					.message(errorMessages.toString())
					.status(HttpStatus.BAD_REQUEST)
					.data(null)
					.build());

		}
		Category category = categoryService.createCategory(categoryDTO);

		return ResponseEntity.ok(
				ResponseObject.builder()
						.message("Category created successfully")
						.status(HttpStatus.CREATED)
						.data(category)
						.build()
		);
	}

	@GetMapping("")
	public ResponseEntity<ResponseObject> getAllCategories(
			@RequestParam(value = "page", required = false) int page,
			@RequestParam(value = "limit", required = false) int limit
	) {
		List<Category> categories = categoryService.getAllCategories();
		return ResponseEntity.ok(
				ResponseObject.builder()
						.message("Categories retrieved successfully")
						.status(HttpStatus.OK)
						.data(categories)
						.build()
		);
	}

	@GetMapping("/{id}")
	public ResponseEntity<ResponseObject> getCategoryById(
			@PathVariable("id") Long categoryId
	) {
		Category existingCategory = categoryService.getCategoryById(categoryId);
		return ResponseEntity.ok(ResponseObject.builder()
				.data(existingCategory)
				.message("Get category information successfully")
				.status(HttpStatus.OK)
				.build());
	}

	@PutMapping("/{id}")
	public ResponseEntity<ResponseObject> updateCategory(
			@PathVariable Long id,
			@Valid @RequestBody CategoryDTO categoryDTO
	) throws Exception {
		categoryService.updateCategory(id, categoryDTO);
		return ResponseEntity.ok(ResponseObject
				.builder()
				.data(categoryService.getCategoryById(id))
				.message("Category updated successfully")
				.build());
	}

	@DeleteMapping("/{id}")
    public ResponseEntity<ResponseObject> deleteCategory(@PathVariable Long id) throws Exception{
        categoryService.deleteCategory(id);
        return ResponseEntity.ok(
                ResponseObject.builder()
                        .status(HttpStatus.OK)
                        .message("Delete category successfully")
                        .build());
    }
}