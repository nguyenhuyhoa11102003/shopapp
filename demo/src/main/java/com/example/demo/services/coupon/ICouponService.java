package com.example.demo.services.coupon;

public interface ICouponService {
	double calculateCouponValue(String couponCode, double totalAmount);
}
