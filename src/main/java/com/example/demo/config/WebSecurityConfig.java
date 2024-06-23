package com.example.demo.config;

import com.example.demo.exceptions.DataNotFoundException;
import com.example.demo.filters.JwtTokenFilter;
import com.example.demo.models.User;
import com.example.demo.services.user.CustomOAuth2UserService;
import com.example.demo.services.user.IUserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;

import java.io.IOException;

@Configuration
@RequiredArgsConstructor
public class WebSecurityConfig {
	private final JwtTokenFilter jwtTokenFilter;
	private final CustomOAuth2UserService oauth2UserService;
	private final IUserService userService;

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

										String.format("%s/categories/**", apiPrefix),
										String.format("%s/products/**", apiPrefix),
										String.format("%s/users/**", apiPrefix),
										String.format("%s/roles/**", apiPrefix),
										String.format("%s/policies/**", apiPrefix),
										String.format("%s/orders", apiPrefix),
										String.format("%s/comments/**", apiPrefix),

										"/login",
										"/home",
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
								.anyRequest().authenticated()
				).csrf(AbstractHttpConfigurer::disable)
				.exceptionHandling(httpSecurityExceptionHandlingConfigurer -> httpSecurityExceptionHandlingConfigurer
						.authenticationEntryPoint((request, response, authException) -> response.sendError(401))
						.accessDeniedHandler((request, response, accessDeniedException) -> response.sendError(403))
				)
				.oauth2Login(oauth2Login -> oauth2Login
						.loginPage("/login")
						.userInfoEndpoint(userInfoEndpoint -> userInfoEndpoint
								.userService(oauth2UserService)
						).successHandler(new AuthenticationSuccessHandler() {
							@Override
							public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
								OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
								try {
									userService.processOAuthPostLogin(oauth2User.getAttribute("email"));
								} catch (DataNotFoundException e) {
									throw new RuntimeException(e);
								}
								response.sendRedirect("/home");
							}
						}));
		return http.build();
	}
}
