package com.example.demo.repositories;

import com.example.demo.models.Category;
import com.example.demo.models.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
	boolean existsByName(String name);

	Page<Product> findAll(Pageable pageable);//phân trang

	List<Product> findByCategory(Category category);

	@Query("SELECT p FROM Product p LEFT JOIN FETCH p.productImages WHERE p.id = :productId")
	Optional<Product> getDetailProduct(@Param("productId") Long productId);

	@Query("SELECT p FROM Product p WHERE p.id IN :productIds")
	List<Product> findProductsByIds(@Param("productIds") List<Long> productIds);

	@Query("SELECT p FROM Product p WHERE (:categoryId IS NULL OR p.category.id = :categoryId) AND ( :keyword LIKE IS NULL OR p.name like %:keyword% OR p.description LIKE %:keyword%)")
	Page<Product> searchProducts(@Param("categoryId") Long categoryId, @Param("keyword") String keyword, Pageable pageable);


}
