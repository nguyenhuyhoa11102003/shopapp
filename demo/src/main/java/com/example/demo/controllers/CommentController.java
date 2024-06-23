package com.example.demo.controllers;


import com.example.demo.components.SecurityUtils;
import com.example.demo.dtos.CommentDTO;
import com.example.demo.models.User;
import com.example.demo.responses.CommentResponse;
import com.example.demo.responses.ResponseObject;
import com.example.demo.services.comment.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/comments")
public class CommentController {

	private final CommentService commentService;
	private final SecurityUtils securityUtils;

	@PostMapping("")
	public ResponseEntity<ResponseObject> insertComment(
			@Valid @RequestBody CommentDTO commentDTO
	) {
		// Insert the new comment
		User loginUser = securityUtils.getLoggedInUser();
		if (!Objects.equals(loginUser.getId(), commentDTO.getUserId())) {
			return ResponseEntity.badRequest().body(
					new ResponseObject(
							"You cannot comment as another user",
							HttpStatus.BAD_REQUEST,
							null));
		}
		commentService.insertComment(commentDTO);
		return ResponseEntity.ok(
				ResponseObject.builder()
						.message("Insert comment successfully")
						.status(HttpStatus.OK)
						.build());
	}


	@GetMapping("")
	public ResponseEntity<ResponseObject> getAllComments(
			@RequestParam(value = "user_id", required = false) Long userId,
			@RequestParam("product_id") Long productId
	) {
		List<CommentResponse> commentResponses;
		if (userId == null) {
			commentResponses = commentService.getCommentsByProduct(productId);
		} else {
			commentResponses = commentService.getCommentsByUserAndProduct(userId, productId);
		}
		return ResponseEntity.ok().body(ResponseObject.builder()
				.message("Get comments successfully")
				.status(HttpStatus.OK)
				.data(commentResponses)
				.build());
	}


	@PutMapping("/{id}")
	public ResponseEntity<ResponseObject> updateComment(
			@PathVariable("id") Long commentId,
			@Valid @RequestBody CommentDTO commentDTO
	) throws Exception {
		User loginUser = securityUtils.getLoggedInUser();
		if (!Objects.equals(loginUser.getId(), commentDTO.getUserId())) {
			return ResponseEntity.badRequest().body(
					new ResponseObject(
							"You cannot update another user's comment",
							HttpStatus.BAD_REQUEST,
							null));

		}
		commentService.updateComment(commentId, commentDTO);
		return ResponseEntity.ok(
				new ResponseObject(
						"Update comment successfully",
						HttpStatus.OK, null));
	}


}
