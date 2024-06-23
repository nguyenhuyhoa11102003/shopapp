package com.example.demo.services.orderdetails;

import com.example.demo.dtos.OrderDetailDTO;
import com.example.demo.exceptions.DataNotFoundException;
import com.example.demo.models.OrderDetail;

import java.util.List;

public interface IOrderDetailService {
	OrderDetail createOrderDetail(OrderDetailDTO newOrderDetail) throws Exception;

	OrderDetail getOrderDetail(Long id) throws DataNotFoundException;

	OrderDetail updateOrderDetail(Long id, OrderDetailDTO newOrderDetailData)
			throws DataNotFoundException;

	void deleteById(Long id);

	List<OrderDetail> findByOrderId(Long orderId);
}
