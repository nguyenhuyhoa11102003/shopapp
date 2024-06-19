package com.example.demo.services.comment;

import com.example.demo.dtos.CommentDTO;
import com.example.demo.exceptions.DataNotFoundException;
import com.example.demo.models.Comment;
import com.example.demo.responses.CommentResponse;

import java.util.List;

public interface ICommentService {
	Comment insertComment(CommentDTO comment);

	void deleteComment(Long commentId);

	void updateComment(Long id, CommentDTO commentDTO) throws DataNotFoundException;

	List<CommentResponse> getCommentsByUserAndProduct(Long userId, Long productId);

	List<CommentResponse> getCommentsByProduct(Long productId);
}
