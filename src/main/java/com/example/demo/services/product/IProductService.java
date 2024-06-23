package com.example.demo.services.product;

import com.example.demo.dtos.ProductDTO;
import com.example.demo.dtos.ProductImageDTO;
import com.example.demo.models.Product;
import com.example.demo.models.ProductImage;
import com.example.demo.responses.product.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

public interface IProductService {
	Product createProduct(ProductDTO productDTO) throws Exception;

	Product getProductById(long id) throws Exception;

	Page<ProductResponse> getAllProducts(String keyword, Long categoryId, PageRequest pageRequest);

	Product updateProduct(long id, ProductDTO productDTO) throws Exception;

	void deleteProduct(long id);

	boolean existsByName(String name);

	ProductImage createProductImage(
			Long productId,
			ProductImageDTO productImageDTO) throws Exception;

	List<Product> findProductsByIds(List<Long> productIds);

	Product likeProduct(Long userId, Long productId) throws Exception;

	Product unlikeProduct(Long userId, Long productId) throws Exception;

	List<ProductResponse> findFavoriteProductsByUserId(Long userId) throws Exception;

	void generateFakeLikes() throws Exception;

}
