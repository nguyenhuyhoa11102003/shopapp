package com.example.demo.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;

@OpenAPIDefinition(
		info = @Info(
				title = "E-commerce api in Java Spring boot",
				version = "1.0.0",
				description = "Ứng dụng ShopApp để training"
		),
		servers = {
				@Server(url = "http://localhost:8088", description = "Local Development Server"),
				@Server(url = "http://45.117.179.16:8088", description = "Production Server"),
		}
)

@Configuration
public class OpenApiConfig {

}