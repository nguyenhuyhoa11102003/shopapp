package com.example.demo.controllers;

import com.example.demo.components.LocalizationUtils;
import com.example.demo.components.SecurityUtils;
import com.example.demo.dtos.OrderDTO;
import com.example.demo.models.Order;
import com.example.demo.models.OrderStatus;
import com.example.demo.responses.OrderResponse;
import com.example.demo.responses.ResponseObject;
import com.example.demo.services.orders.IOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.FieldError;

import com.example.demo.models.User;

import java.util.List;


@RestController
@RequestMapping("${api.prefix}/orders")
@RequiredArgsConstructor
public class OrderController {
	private final IOrderService orderService;
	private final LocalizationUtils localizationUtils;
	private final SecurityUtils securityUtils;


	@PostMapping("")
	public ResponseEntity<ResponseObject> createOrder(
			@Valid @RequestBody OrderDTO orderDTO,
			BindingResult result
	) throws Exception {
		if (result.hasErrors()) {
			List<String> errorMessages = result.getFieldErrors()
					.stream()
					.map(FieldError::getDefaultMessage)
					.toList();

			return ResponseEntity.badRequest().body(ResponseObject.builder()
					.status(HttpStatus.BAD_REQUEST)
					.data(null)
					.message(errorMessages.toString())
					.build());
		}

		User loginUser = securityUtils.getLoggedInUser();
		if (orderDTO.getUserId() == null) {
			orderDTO.setUserId(loginUser.getId());
		}
		Order orderResponse = orderService.createOrder(orderDTO);
		return ResponseEntity.ok(ResponseObject.builder()
				.message("Insert order successfully")
				.data(OrderResponse.fromOrder(orderResponse))
				.status(HttpStatus.OK)
				.build());
	}


	@GetMapping("/{id}")
	public ResponseEntity<ResponseObject> getOrderById(@PathVariable Long id) {
		Order order = orderService.getOrderById(id);
		return ResponseEntity.ok(ResponseObject.builder()
				.message("Get order successfully")
				.data(OrderResponse.fromOrder(order))
				.status(HttpStatus.OK)
				.build());
	}


	@PutMapping("/cancel/{id}")
	public ResponseEntity<ResponseObject> cancelOrder(@PathVariable Long id) throws Exception {
		Order order = orderService.getOrderById(id);
		User loginUser = securityUtils.getLoggedInUser();

		if (loginUser.getId() != order.getUser().getId()) {
			return ResponseEntity.badRequest().body(ResponseObject.builder()
					.status(HttpStatus.BAD_REQUEST)
					.data(null)
					.message("You do not have permission to cancel this order")
					.build());
		}

		if (order.getStatus().equals(OrderStatus.DELIVERED) ||
				order.getStatus().equals(OrderStatus.SHIPPED) ||
				order.getStatus().equals(OrderStatus.PROCESSING)) {

			String message = "You cannot cancel an order with status: " + order.getStatus();
			return ResponseEntity.badRequest().body(ResponseObject.builder()
					.status(HttpStatus.BAD_REQUEST)
					.data(null)
					.message(message)
					.build());
		}
		OrderDTO orderDTO = OrderDTO.builder()
				.userId(order.getUser().getId())
				.status(OrderStatus.CANCELLED)
				.build();

		orderService.updateOrder(id, orderDTO);
		return ResponseEntity.ok(ResponseObject.builder()
				.message("Cancel order successfully")
				.data(OrderResponse.fromOrder(order))
				.status(HttpStatus.OK)
				.build());
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<ResponseObject> deleteOrder(@Valid @PathVariable Long id) {
		//xóa mềm => cập nhật trường active = false
		orderService.deleteOrder(id);
		return ResponseEntity.ok(
				ResponseObject.builder()
						.message("Delete order successfully")
						.build()
		);
	}

	@GetMapping("/get-orders-by-keyword")
	public ResponseEntity<ResponseObject> getOrdersByKeyword(
			@RequestParam(defaultValue = "", required = false) String keyword,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int limit
	) {
		// Tạo Pageable từ thông tin trang và giới hạn
		PageRequest pageRequest = PageRequest.of(
				page, limit,
				Sort.by("id").ascending()
		);
		Page<OrderResponse> orderPage = orderService
				.getOrdersByKeyword(keyword, pageRequest)
				.map(OrderResponse::fromOrder);
		// Lấy tổng số trang
		int totalPages = orderPage.getTotalPages();
		List<OrderResponse> orderResponses = orderPage.getContent();
		return ResponseEntity.ok().body(ResponseObject.builder()
				.message("Get orders successfully")
				.status(HttpStatus.OK)
				.data(orderResponses)
				.build());
	}


	@GetMapping("/user/{user_id}")
	public ResponseEntity<ResponseObject> getOrdersByUserId(@Valid @PathVariable("user_id") Long userId) {
		User loginUser = securityUtils.getLoggedInUser();
		boolean isUserIdBlank = userId == null || userId <= 0;
		List<OrderResponse> orderResponses = orderService.findByUserId(isUserIdBlank ? loginUser.getId() : userId);
		return ResponseEntity.ok(ResponseObject.builder()
				.message("Get orders successfully")
				.data(orderResponses)
				.status(HttpStatus.OK)
				.build());
	}

}
