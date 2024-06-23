package com.example.demo.services.comment;

import com.example.demo.dtos.CommentDTO;
import com.example.demo.exceptions.DataNotFoundException;
import com.example.demo.models.Comment;
import com.example.demo.models.User;
import com.example.demo.models.Product;
import com.example.demo.repositories.CommentRepository;
import com.example.demo.repositories.ProductRepository;
import com.example.demo.repositories.UserRepository;
import com.example.demo.responses.CommentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@Slf4j
@RequiredArgsConstructor
public class CommentService implements ICommentService {
	private final CommentRepository commentRepository;
	private final UserRepository userRepository;
	private final ProductRepository productRepository;

	@Override
	@Transactional
	public Comment insertComment(CommentDTO commentDTO) {
		User user = userRepository.findById(commentDTO.getUserId()).orElse(null);
		Product product = productRepository.findById(commentDTO.getProductId()).orElse(null);

		if (user == null || product == null) {
			throw new IllegalArgumentException("User or product not found");
		}

		Comment comment = Comment
				.builder()
				.product(product)
				.user(user)
				.content(commentDTO.getContent())
				.build();
		return commentRepository.save(comment);
	}

	@Override
	@Transactional
	public void deleteComment(Long commentId) {
		this.commentRepository.deleteById(commentId);
	}

	@Override
	public void updateComment(Long id, CommentDTO commentDTO) throws DataNotFoundException {
		Comment existingComment = commentRepository.findById(id)
				.orElseThrow(() -> new DataNotFoundException("Comment not found"));
		existingComment.setContent(commentDTO.getContent());
		commentRepository.save(existingComment);
	}

	@Override
	public List<CommentResponse> getCommentsByUserAndProduct(Long userId, Long productId) {
		List<Comment> comments = commentRepository.findByUserIdAndProductId(userId, productId);
		return comments.stream()
				.map(CommentResponse::fromComment)
				.toList();
	}

	@Override
	public List<CommentResponse> getCommentsByProduct(Long productId) {
		List<Comment> comments = commentRepository.findByProductId(productId);
		return comments.stream()
				.map(CommentResponse::fromComment)
				.toList();
	}
}
