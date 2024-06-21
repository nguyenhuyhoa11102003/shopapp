package com.example.demo.services.orders;

import com.example.demo.dtos.CartItemDTO;
import com.example.demo.dtos.OrderDTO;
import com.example.demo.exceptions.DataNotFoundException;
import com.example.demo.models.*;
import com.example.demo.repositories.*;
import com.example.demo.responses.OrderResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Service
@Slf4j
@RequiredArgsConstructor
public class OrderService implements IOrderService {
	private final OrderRepository orderRepository;
	private final UserRepository userRepository;
	private final ProductRepository productRepository;
	private final CouponRepository couponRepository;
	private final OrderDetailRepository orderDetailRepository;
	private final ModelMapper modelMapper;

	@Override
	public Order createOrder(OrderDTO orderDTO) throws Exception {

		User user = userRepository
				.findById(orderDTO.getUserId())
				.orElseThrow(() -> new DataNotFoundException("Cannot find user with id: " + orderDTO.getUserId()));

		modelMapper.typeMap(OrderDTO.class, Order.class)
				.addMappings(mapper -> mapper.skip(Order::setId));

		Order order = new Order();
		modelMapper.map(orderDTO, order);
		order.setUser(user);
		order.setOrderDate(LocalDateTime.now());
		order.setStatus(OrderStatus.PENDING);
		LocalDate shippingDate = orderDTO.getShippingDate() == null
				? LocalDate.now() : orderDTO.getShippingDate();

		if (shippingDate.isBefore(LocalDate.now())) {
			throw new DataNotFoundException("Date must be at least today !");
		}

		order.setShippingDate(shippingDate);
		order.setActive(true);
		order.setTotalMoney(orderDTO.getTotalMoney());

		List<OrderDetail> orderDetails = new ArrayList<>();

		for (CartItemDTO cartItemDTO : orderDTO.getCartItems()) {
			OrderDetail orderDetail = new OrderDetail();
			orderDetail.setOrder(order);

			// Lấy thông tin sản phẩm từ cartItemDTO
			Long productId = cartItemDTO.getProductId();
			int quantity = cartItemDTO.getQuantity();

			// Tìm thông tin sản phẩm từ cơ sở dữ liệu (hoặc sử dụng cache nếu cần)
			Product product = productRepository.findById(productId)
					.orElseThrow(() -> new DataNotFoundException("Product not found with id: " + productId));

			// Đặt thông tin cho OrderDetail
			orderDetail.setProduct(product);
			orderDetail.setNumberOfProducts(quantity);
			orderDetail.setPrice(product.getPrice());
			orderDetail.setTotalMoney(product.getPrice() * quantity);
			orderDetails.add(orderDetail);
		}
		//coupon
		String couponCode = orderDTO.getCouponCode();
		if (!couponCode.isEmpty()) {
			Coupon coupon = couponRepository.findByCode(couponCode)
					.orElseThrow(() -> new IllegalArgumentException("Coupon not found"));

			if (!coupon.isActive()) {
				throw new IllegalArgumentException("Coupon is not active");
			}

			order.setCoupon(coupon);
		} else {
			order.setCoupon(null);
		}
		order.setOrderDetails(orderDetails);
		this.orderRepository.save(order);
		return order;
	}

	@Override
	public Order getOrderById(Long orderId) {
		return this.orderRepository.findById(orderId).orElse(null);
	}

	@Override
	public Order updateOrder(Long id, OrderDTO orderDTO) throws DataNotFoundException {

		Order order = this.orderRepository.findById(id).orElseThrow(() -> new DataNotFoundException("Cannot find order with id: " + id));
		User existingUser = userRepository.findById(
				orderDTO.getUserId()).orElseThrow(() -> new DataNotFoundException("Cannot find user with id: " + id));

		if (orderDTO.getUserId() != null) {
			User user = new User();
			user.setId(orderDTO.getUserId());
			order.setUser(user);
		}

		if (orderDTO.getFullName() != null && !orderDTO.getFullName().trim().isEmpty()) {
			order.setFullName(orderDTO.getFullName().trim());
		}

		if (orderDTO.getEmail() != null && !orderDTO.getEmail().trim().isEmpty()) {
			order.setEmail(orderDTO.getEmail().trim());
		}

		if (orderDTO.getPhoneNumber() != null && !orderDTO.getPhoneNumber().trim().isEmpty()) {
			order.setPhoneNumber(orderDTO.getPhoneNumber().trim());
		}

		if (orderDTO.getStatus() != null && !orderDTO.getStatus().trim().isEmpty()) {
			order.setStatus(orderDTO.getStatus().trim());
		}

		if (orderDTO.getAddress() != null && !orderDTO.getAddress().trim().isEmpty()) {
			order.setAddress(orderDTO.getAddress().trim());
		}

		if (orderDTO.getNote() != null && !orderDTO.getNote().trim().isEmpty()) {
			order.setNote(orderDTO.getNote().trim());
		}

		if (orderDTO.getTotalMoney() != null) {
			order.setTotalMoney(orderDTO.getTotalMoney());
		}

		if (orderDTO.getShippingMethod() != null && !orderDTO.getShippingMethod().trim().isEmpty()) {
			order.setShippingMethod(orderDTO.getShippingMethod().trim());
		}

		if (orderDTO.getShippingAddress() != null && !orderDTO.getShippingAddress().trim().isEmpty()) {
			order.setShippingAddress(orderDTO.getShippingAddress().trim());
		}

		if (orderDTO.getShippingDate() != null) {
			order.setShippingDate(orderDTO.getShippingDate());
		}

		if (orderDTO.getPaymentMethod() != null && !orderDTO.getPaymentMethod().trim().isEmpty()) {
			order.setPaymentMethod(orderDTO.getPaymentMethod().trim());
		}

		order.setUser(existingUser);
		return this.orderRepository.save(order);
	}

	@Override
	public void deleteOrder(Long orderId) {
		Order order = this.orderRepository.findById(orderId).orElse(null);
		if (order != null) {
			order.setActive(false);
			this.orderRepository.save(order);
		}
	}

	@Override
	public List<OrderResponse> findByUserId(Long userId) {
		List<Order> orders = this.orderRepository.findByUserId(userId);
		return orders.stream().map(OrderResponse::fromOrder).toList();
	}

	@Override
	public Page<Order> getOrdersByKeyword(String keyword, Pageable pageable) {
		return orderRepository.findByKeyword(keyword, pageable);
	}
}
