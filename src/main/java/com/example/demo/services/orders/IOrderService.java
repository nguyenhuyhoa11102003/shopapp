package com.example.demo.services.orders;

import com.example.demo.dtos.OrderDTO;
import com.example.demo.exceptions.DataNotFoundException;
import com.example.demo.models.Order;
import com.example.demo.responses.OrderResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IOrderService {
	Order createOrder(OrderDTO orderDTO) throws Exception;

	Order getOrderById(Long orderId);

	Order updateOrder(Long id, OrderDTO orderDTO) throws DataNotFoundException;

	void deleteOrder(Long orderId);

	List<OrderResponse> findByUserId(Long userId);

	Page<Order> getOrdersByKeyword(String keyword, Pageable pageable);
}
