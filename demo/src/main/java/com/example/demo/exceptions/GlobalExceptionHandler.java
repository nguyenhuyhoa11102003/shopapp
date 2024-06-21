package com.example.demo.exceptions;

import com.example.demo.responses.ResponseObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
	@ExceptionHandler(Exception.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public ResponseEntity<ResponseObject> handleGeneralException(Exception exception) {
		log.error(exception.getMessage());
		return ResponseEntity.internalServerError().body(
				ResponseObject.builder()
						.status(HttpStatus.INTERNAL_SERVER_ERROR)
						.message(exception.getMessage())
						.build()
		);
	}

	@ExceptionHandler(DataNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public ResponseEntity<?> handleResourceNotFoundException(DataNotFoundException exception) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ResponseObject.builder()
				.status(HttpStatus.NOT_FOUND)
				.message(exception.getMessage())
				.build());
	}

}
