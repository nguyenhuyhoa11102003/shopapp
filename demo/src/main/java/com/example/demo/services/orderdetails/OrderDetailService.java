package com.example.demo.services.orderdetails;

import com.example.demo.dtos.OrderDetailDTO;
import com.example.demo.exceptions.DataNotFoundException;
import com.example.demo.models.Order;
import com.example.demo.models.OrderDetail;
import com.example.demo.models.Product;
import com.example.demo.repositories.OrderDetailRepository;
import com.example.demo.repositories.OrderRepository;
import com.example.demo.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@Slf4j
@RequiredArgsConstructor
public class OrderDetailService implements IOrderDetailService {
	private final OrderDetailRepository orderDetailRepository;
	private final OrderRepository orderRepository;
	private final ProductRepository productRepository;

	@Override
	public OrderDetail createOrderDetail(OrderDetailDTO orderDetailDTO) throws Exception {
		//tìm xem orderId có tồn tại ko
		Order order = orderRepository.findById(orderDetailDTO.getOrderId())
				.orElseThrow(() -> new DataNotFoundException(
						"Cannot find Order with id : " + orderDetailDTO.getOrderId()));

		// Tìm Product theo id
		Product product = productRepository.findById(orderDetailDTO.getProductId())
				.orElseThrow(() -> new DataNotFoundException(
						"Cannot find product with id: " + orderDetailDTO.getProductId()));

		OrderDetail orderDetail = OrderDetail.builder()
				.order(order)
				.product(product)
				.numberOfProducts(orderDetailDTO.getNumberOfProducts())
				.price(orderDetailDTO.getPrice())
				.totalMoney(orderDetailDTO.getTotalMoney())
				.color(orderDetailDTO.getColor())
				.build();

		//lưu vào db
		return orderDetailRepository.save(orderDetail);
	}

	@Override
	public OrderDetail getOrderDetail(Long id) throws DataNotFoundException {
		return orderDetailRepository.findById(id)
				.orElseThrow(() -> new DataNotFoundException("Cannot find OrderDetail with id: " + id));
	}

	@Override
	public OrderDetail updateOrderDetail(Long id, OrderDetailDTO orderDetailDTO) throws DataNotFoundException {

		//tìm xem order detail có tồn tại ko đã
		OrderDetail existingOrderDetail = orderDetailRepository.findById(id)
				.orElseThrow(() -> new DataNotFoundException("Cannot find OrderDetail with id: " + id));

		Order existingOrder = orderRepository.findById(orderDetailDTO.getOrderId())
				.orElseThrow(() -> new DataNotFoundException("Cannot find order with id: " + id));

		Product existingProduct = productRepository.findById(orderDetailDTO.getProductId())
				.orElseThrow(() -> new DataNotFoundException(
						"Cannot find product with id: " + orderDetailDTO.getProductId()));


		existingOrderDetail.setPrice(orderDetailDTO.getPrice());
		existingOrderDetail.setNumberOfProducts(orderDetailDTO.getNumberOfProducts());
		existingOrderDetail.setTotalMoney(orderDetailDTO.getTotalMoney());
		existingOrderDetail.setColor(orderDetailDTO.getColor());
		existingOrderDetail.setOrder(existingOrder);
		existingOrderDetail.setProduct(existingProduct);

		return orderDetailRepository.save(existingOrderDetail);
	}

	@Override
	@Transactional
	public void deleteById(Long id) {
		orderDetailRepository.deleteById(id);
	}

	@Override
	public List<OrderDetail> findByOrderId(Long orderId) {
		return orderDetailRepository.findByOrderId(orderId);
	}
}
