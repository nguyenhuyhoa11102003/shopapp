package com.example.demo.config;

import com.example.demo.filters.JwtTokenFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebSecurityConfig {
	private final JwtTokenFilter jwtTokenFilter;

	@Value("${api.prefix}")
	private String apiPrefix;

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
				.addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class)
				.authorizeRequests(authorizeRequests ->
						authorizeRequests
								.requestMatchers(
										String.format("%s/users/register", apiPrefix),
										String.format("%s/users/login", apiPrefix),
										//healthcheck
										String.format("%s/healthcheck/**", apiPrefix),
										//swagger
										//"/v3/api-docs",
										//"/v3/api-docs/**",
										"/api-docs",
										"/api-docs/**",
										"/swagger-resources",
										"/swagger-resources/**",
										"/configuration/ui",
										"/configuration/security",
										"/swagger-ui/**",
										"/swagger-ui.html",
										"/webjars/swagger-ui/**",
										"/swagger-ui/index.html"

								)
								.permitAll()
								.anyRequest().permitAll()
				).csrf(AbstractHttpConfigurer::disable);
		return http.build();
	}
}
