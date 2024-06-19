package com.example.demo.responses;

import com.example.demo.models.Comment;
import com.example.demo.responses.user.UserResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommentResponse extends BaseResponse {
	@JsonProperty("id")
	private Long id;

	@JsonProperty("content")
	private String content;

	@JsonProperty("user")
	private UserResponse user;

	@JsonProperty("product_id")
	private Long productId;

	public static CommentResponse fromComment(Comment comment) {
		UserResponse userResponse = UserResponse.fromUser(comment.getUser());
		CommentResponse commentResponse = CommentResponse.builder()
				.id(comment.getId())
				.content(comment.getContent())
				.user(userResponse)
				.productId(comment.getProduct().getId())
				.build();
		commentResponse.setCreatedAt(comment.getCreatedAt());
		return commentResponse;
	}
}
