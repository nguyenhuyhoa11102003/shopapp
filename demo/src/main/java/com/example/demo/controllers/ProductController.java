package com.example.demo.controllers;


import com.example.demo.components.LocalizationUtils;
import com.example.demo.dtos.ProductDTO;
import com.example.demo.dtos.ProductImageDTO;
import com.example.demo.models.Product;
import com.example.demo.models.ProductImage;
import com.example.demo.responses.ResponseObject;
import com.example.demo.responses.product.ProductListResponse;
import com.example.demo.responses.product.ProductResponse;
import com.example.demo.services.product.IProductRedisService;
import com.example.demo.services.product.IProductService;
import com.example.demo.utils.FileUtils;
import com.example.demo.utils.MessageKeys;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.FieldError;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("${api.prefix}/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {
	private final IProductService productService;
	private final LocalizationUtils localizationUtils;
	private final IProductRedisService productRedisService;

	@PostMapping("")
	public ResponseEntity<ResponseObject> createProduct(@Valid @RequestBody ProductDTO productDTO, BindingResult result) throws Exception {
		log.info("Creating product: {}", productDTO);
		if (result.hasErrors()) {
			List<String> errorMessages = result.getFieldErrors()
					.stream()
					.map(FieldError::getDefaultMessage)
					.toList();
			return ResponseEntity.badRequest().body(
					ResponseObject.builder()
							.message(String.join("; ", errorMessages))
							.status(HttpStatus.BAD_REQUEST)
							.build()
			);
		}
		Product newProduct = productService.createProduct(productDTO);
		return ResponseEntity.ok(ResponseObject.builder()
				.message("Create new product successfully")
				.status(HttpStatus.CREATED)
				.data(newProduct)
				.build());
	}


	@PostMapping(value = "uploads/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<ResponseObject> uploadImages(@PathVariable("id") Long productId, @ModelAttribute("files") List<MultipartFile> files) throws Exception {
		Product existingProduct = productService.getProductById(productId);
		files = files == null ? new ArrayList<MultipartFile>() : files;
		if (files.size() > ProductImage.MAXIMUM_IMAGES_PER_PRODUCT) {
			return ResponseEntity.badRequest().body(
					ResponseObject.builder()
							.message(localizationUtils
									.getLocalizedMessage(MessageKeys.UPLOAD_IMAGES_MAX_5))
							.build()
			);
		}
		List<ProductImage> productImages = new ArrayList<>();
		for (MultipartFile file : files) {
			if (file.getSize() == 0) {
				continue;
			}
			// check size image > 10MB
			if (file.getSize() > 10 * 1024 * 1024) { // Kích thước > 10MB
				return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
						.body(ResponseObject.builder()
								.message(localizationUtils
										.getLocalizedMessage(MessageKeys.UPLOAD_IMAGES_FILE_LARGE))
								.status(HttpStatus.PAYLOAD_TOO_LARGE)
								.build());
			}
			String contentType = file.getContentType();
			if (contentType == null || !contentType.startsWith("image/")) {
				return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
						.body(ResponseObject.builder()
								.message(localizationUtils
										.getLocalizedMessage(MessageKeys.UPLOAD_IMAGES_FILE_MUST_BE_IMAGE))
								.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
								.build());
			}

			// Lưu file và cập nhật thumbnail trong DTO
			String filename = FileUtils.storeFile(file);

			ProductImage productImage = productService.createProductImage(
					existingProduct.getId(),
					ProductImageDTO.builder()
							.imageUrl(filename)
							.build()
			);
			productImages.add(productImage);
		}

		return ResponseEntity.ok(ResponseObject
				.builder()
				.message("Upload image successfully")
				.status(HttpStatus.CREATED)
				.build());
	}

	@GetMapping("/images/{imageName}")
	public ResponseEntity<?> viewImage(@PathVariable String imageName) {
		try {
			java.nio.file.Path imagePath = Paths.get("uploads/" + imageName);
			UrlResource resource = new UrlResource(imagePath.toUri());

			if (resource.exists()) {
				return ResponseEntity.ok()
						.contentType(MediaType.IMAGE_JPEG)
						.body(resource);
			} else {
				log.info(imageName + " not found");
				return ResponseEntity.ok()
						.contentType(MediaType.IMAGE_JPEG)
						.body(new UrlResource(Paths.get("uploads/notfound.jpeg").toUri()));
			}
		} catch (Exception e) {
			log.error("Error occurred while retrieving image: " + e.getMessage());
			return ResponseEntity.notFound().build();
		}
	}


	@GetMapping("")
	public ResponseEntity<ResponseObject> getProducts(
			@RequestParam(defaultValue = "") String keyword,
			@RequestParam(defaultValue = "0", name = "category_id") Long categoryId,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int limit
	) throws JsonProcessingException {
		int totalPages = 0;
		PageRequest pageRequest = PageRequest.of(
				page,
				limit,
				Sort.by("id").ascending()
		);
		log.info(String.format("keyword = %s, category_id = %d, page = %d, limit = %d",
				keyword, categoryId, page, limit));

		List<ProductResponse> productResponses = productRedisService.getAllProducts(keyword, categoryId, pageRequest);
		if (productResponses != null && !productResponses.isEmpty()) {
			totalPages = productResponses.get(0).getTotalPages();
		}

		if (productResponses == null) {
			Page<ProductResponse> productPage = productService
					.getAllProducts(keyword, categoryId, pageRequest);

			for (ProductResponse product : productPage) {
				product.setTotalPages(productPage.getTotalPages());
			}
			totalPages = productPage.getTotalPages();
			productResponses = productPage.getContent();
			for (ProductResponse product : productResponses) {
				product.setTotalPages(totalPages);
			}

			productRedisService.saveAllProducts(productResponses, keyword, categoryId, pageRequest);
		}

		ProductListResponse productListResponse = ProductListResponse
				.builder()
				.products(productResponses)
				.totalPages(totalPages)
				.build();

		return ResponseEntity.ok(ResponseObject.builder()
				.message("Get products successfully")
				.status(HttpStatus.OK)
				.data(productListResponse)
				.build());

	}

	@GetMapping("/{id}")
	public ResponseEntity<ResponseObject> getProductById(
			@PathVariable("id") Long productId
	) throws Exception {
		Product existingProduct = productService.getProductById(productId);
		return ResponseEntity.ok(ResponseObject.builder()
				.data(ProductResponse.fromProduct(existingProduct))
				.message("Get detail product successfully")
				.status(HttpStatus.OK)
				.build());
	}


	@GetMapping("/by-ids")
	public ResponseEntity<ResponseObject> getProductByIds(@RequestParam("ids") String ids) {
		List<Long> productIds = Arrays.stream(ids.split(","))
				.map(Long::parseLong)
				.toList();

		List<Product> products = productService.findProductsByIds(productIds);

		return ResponseEntity.ok(ResponseObject.builder()
				.data(products.stream()
						.map(ProductResponse::fromProduct)
						.toList())
				.message("Get products by ids successfully")
				.status(HttpStatus.OK)
				.build());
	}


	@DeleteMapping("/{id}")
	public ResponseEntity<ResponseObject> deleteProduct(@PathVariable long id) {
		productService.deleteProduct(id);
		return ResponseEntity.ok(ResponseObject.builder()
				.data(null)
				.message(String.format("Product with id = %d deleted successfully", id))
				.status(HttpStatus.OK)
				.build());
	}

	@PutMapping("/{id}")
	public ResponseEntity<ResponseObject> updateProduct(
			@PathVariable long id,
			@RequestBody ProductDTO productDTO) throws Exception {
		Product updatedProduct = productService.updateProduct(id, productDTO);
		return ResponseEntity.ok(ResponseObject.builder()
				.data(updatedProduct)
				.message("Update product successfully")
				.status(HttpStatus.OK)
				.build());
	}


}
