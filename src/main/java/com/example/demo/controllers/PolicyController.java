package com.example.demo.controllers;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/policies")
public class PolicyController {
	@GetMapping("/privacy-policy")
	public String privacyPolicy() {
		return "privacy-policy";
	}

	@GetMapping("/terms-of-service.html")
	public String termsOfService() {
		return "terms-of-service.html";
	}
}
