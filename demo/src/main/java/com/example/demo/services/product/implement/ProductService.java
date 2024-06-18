package com.example.demo.services.product.implement;

import com.example.demo.dtos.ProductDTO;
import com.example.demo.dtos.ProductImageDTO;
import com.example.demo.exceptions.DataNotFoundException;
import com.example.demo.exceptions.InvalidParamException;
import com.example.demo.models.Category;
import com.example.demo.models.Product;
import com.example.demo.models.ProductImage;
import com.example.demo.repositories.CategoryRepository;
import com.example.demo.repositories.ProductRepository;
import com.example.demo.repositories.ProductImageRepository;
import com.example.demo.responses.product.ProductResponse;
import com.example.demo.services.product.IProductService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService implements IProductService {
	private final ProductRepository productRepository;
	private final CategoryRepository categoryRepository;
	private final ProductImageRepository productImageRepository;

	@Transactional
	@Override
	public Product createProduct(ProductDTO productDTO) throws DataNotFoundException {

		Category category = categoryRepository.findById(productDTO.getCategoryId())
				.orElseThrow(() -> new DataNotFoundException("Cannot find category with id: " + productDTO.getCategoryId()));

		Product newProduct = Product.builder()
				.name(productDTO.getName())
				.thumbnail(productDTO.getThumbnail())
				.description(productDTO.getDescription())
				.category(category)
				.build();

		return productRepository.save(newProduct);
	}

	@Override
	public Product getProductById(long productId) throws Exception {
		Optional<Product> optionalProduct = productRepository.getDetailProduct(productId);
		if (optionalProduct.isEmpty()) {
			throw new DataNotFoundException("Cannot find product with id: " + productId);
		}
		return optionalProduct.get();
	}

	@Override
	public Page<ProductResponse> getAllProducts(String keyword, Long categoryId, PageRequest pageRequest) {
		Page<Product> productsPage;
		productsPage = productRepository.searchProducts(categoryId, keyword, pageRequest);
		return productsPage.map(ProductResponse::fromProduct);
	}

	@Override
	public Product updateProduct(long id, ProductDTO productDTO) throws Exception {
		Product existingProduct = getProductById(id);

		if (existingProduct != null) {
			Category existingCategory = categoryRepository
					.findById(productDTO.getCategoryId())
					.orElseThrow(() ->
							new DataNotFoundException(
									"Cannot find category with id: " + productDTO.getCategoryId()));

			if (productDTO.getName() != null && !productDTO.getName().isEmpty()) {
				existingProduct.setName(productDTO.getName());
			}
			existingProduct.setCategory(existingCategory);
			if (productDTO.getPrice() >= 0) {
				existingProduct.setPrice(productDTO.getPrice());
			}
			if (productDTO.getDescription() != null &&
					!productDTO.getDescription().isEmpty()) {
				existingProduct.setDescription(productDTO.getDescription());
			}
			if (productDTO.getThumbnail() != null &&
					!productDTO.getThumbnail().isEmpty()) {
				existingProduct.setDescription(productDTO.getThumbnail());
			}
			return productRepository.save(existingProduct);
		}
		return null;
	}

	@Override
	@Transactional
	public void deleteProduct(long id) {
		Optional<Product> optionalProduct = productRepository.findById(id);
		optionalProduct.ifPresent(productRepository::delete);
	}

	@Override
	public boolean existsByName(String name) {
		return productRepository.existsByName(name);
	}

	@Override
	public ProductImage createProductImage(Long productId, ProductImageDTO productImageDTO) throws Exception {
		Product existingProduct = productRepository
				.findById(productId)
				.orElseThrow(() ->
						new DataNotFoundException(
								"Cannot find product with id: " + productImageDTO.getProductId()));

		ProductImage newProductImage = ProductImage.builder()
				.product(existingProduct)
				.imageUrl(productImageDTO.getImageUrl())
				.build();

		int size = productImageRepository.findByProductId(productId).size();
		if (size < ProductImage.MAXIMUM_IMAGES_PER_PRODUCT) {
			throw new InvalidParamException(
					"Number of images must be <= "
							+ ProductImage.MAXIMUM_IMAGES_PER_PRODUCT);
		}
		if (existingProduct.getThumbnail() == null) {
			existingProduct.setThumbnail(newProductImage.getImageUrl());
		}

		productRepository.save(existingProduct);
		return productImageRepository.save(newProductImage);
	}

	@Override
	public List<Product> findProductsByIds(List<Long> productIds) {
		return productRepository.findProductsByIds(productIds);
	}

	@Override
	public Product likeProduct(Long userId, Long productId) throws Exception {
		return null;
	}

	@Override
	public Product unlikeProduct(Long userId, Long productId) throws Exception {
		return null;
	}

	@Override
	public List<ProductResponse> findFavoriteProductsByUserId(Long userId) throws Exception {
		return null;
	}

	@Override
	public void generateFakeLikes() throws Exception {

	}
}
