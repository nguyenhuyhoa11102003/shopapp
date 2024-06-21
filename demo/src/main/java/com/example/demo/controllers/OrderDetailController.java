package com.example.demo.controllers;


import com.example.demo.dtos.OrderDetailDTO;
import com.example.demo.exceptions.DataNotFoundException;
import com.example.demo.models.OrderDetail;
import com.example.demo.responses.OrderDetailResponse;
import com.example.demo.responses.ResponseObject;
import com.example.demo.services.orderdetails.OrderDetailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/order_details")
@RequiredArgsConstructor
public class OrderDetailController {
	private final OrderDetailService orderDetailService;
	@PostMapping("")
	public ResponseEntity<ResponseObject> createOrderDetail(
			@Valid @RequestBody OrderDetailDTO orderDetailDTO) throws Exception {
		OrderDetail newOrderDetail = orderDetailService.createOrderDetail(orderDetailDTO);
		OrderDetailResponse orderDetailResponse = OrderDetailResponse.fromOrderDetail(newOrderDetail);
		return ResponseEntity.ok().body(
				ResponseObject.builder()
						.message("Create order detail successfully")
						.status(HttpStatus.CREATED)
						.data(orderDetailResponse)
						.build()
		);
	}

	@GetMapping("/{id}")
	public ResponseEntity<?> getOrderDetail(
			@Valid @PathVariable("id") Long id) throws DataNotFoundException {
		OrderDetail orderDetail = orderDetailService.getOrderDetail(id);
		OrderDetailResponse orderDetailResponse = OrderDetailResponse.fromOrderDetail(orderDetail);
		return ResponseEntity.ok().body(
				ResponseObject.builder()
						.message("Get order detail successfully")
						.status(HttpStatus.OK)
						.data(orderDetailResponse)
						.build()
		);
	}


	@GetMapping("/order/{orderId}")
	public ResponseEntity<ResponseObject> getOrderDetails(
			@Valid @PathVariable("orderId") Long orderId
	) {
		List<OrderDetail> orderDetails = orderDetailService.findByOrderId(orderId);
		List<OrderDetailResponse> orderDetailResponses = orderDetails
				.stream()
				.map(OrderDetailResponse::fromOrderDetail)
				.toList();
		return ResponseEntity.ok().body(
				ResponseObject.builder()
						.message("Get order details by orderId successfully")
						.status(HttpStatus.OK)
						.data(orderDetailResponses)
						.build()
		);
	}

	@PutMapping("/{id}")
	public ResponseEntity<ResponseObject> updateOrderDetail(
			@Valid @PathVariable("id") Long id,
			@RequestBody OrderDetailDTO orderDetailDTO) throws DataNotFoundException, Exception {
		OrderDetail orderDetail = orderDetailService.updateOrderDetail(id, orderDetailDTO);
		return ResponseEntity.ok().body(ResponseObject
				.builder()
				.data(orderDetail)
				.message("Update order detail successfully")
				.status(HttpStatus.OK)
				.build());
	}


	@DeleteMapping("/{id}")
	public ResponseEntity<ResponseObject> deleteOrderDetail(
			@Valid @PathVariable("id") Long id) {
		orderDetailService.deleteById(id);
		return ResponseEntity.ok()
				.body(ResponseObject.builder()
						.message("Delete order detail successfully")
						.build());
	}

}
