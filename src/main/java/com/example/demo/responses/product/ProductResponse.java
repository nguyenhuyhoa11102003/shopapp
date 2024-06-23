package com.example.demo.responses.product;

import com.example.demo.models.Product;
import com.example.demo.models.ProductImage;
import com.example.demo.responses.BaseResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductResponse extends BaseResponse {

	private Long id;
	private String name;
	private Float price;
	private String thumbnail;
	private String description;
	// Thêm trường totalPages
	private int totalPages;

	@JsonProperty("product_images")
	private List<ProductImage> productImages = new ArrayList<>();

	@JsonProperty("category_id")
	private Long categoryId;

	public static ProductResponse fromProduct(Product product) {
		ProductResponse productResponse = ProductResponse.builder()
				.id(product.getId())
				.name(product.getName())
				.price(product.getPrice())
				.thumbnail(product.getThumbnail())
				//				                .comments(comments.stream().map(CommentResponse::fromComment).toList()) // Collect sorted comments into a list
				//                .favorites(favorites.stream().map(FavoriteResponse::fromFavorite).toList())
				.description(product.getDescription())
				.categoryId(product.getCategory().getId())
				.productImages(product.getProductImages())
				.totalPages(0)
				.build();
		productResponse.setCreatedAt(product.getCreatedAt());
		productResponse.setUpdatedAt(product.getUpdatedAt());
		return productResponse;
	}


}
