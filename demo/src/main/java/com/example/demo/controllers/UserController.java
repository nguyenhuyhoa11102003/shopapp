package com.example.demo.controllers;


import com.example.demo.components.LocalizationUtils;
import com.example.demo.dtos.RefreshTokenDTO;
import com.example.demo.dtos.UserDTO;
import com.example.demo.dtos.UserLoginDTO;
import com.example.demo.models.Token;
import com.example.demo.models.User;
import com.example.demo.responses.ResponseObject;
import com.example.demo.responses.user.LoginResponse;
import com.example.demo.responses.user.UserResponse;
import com.example.demo.services.token.TokenService;
import com.example.demo.utils.MessageKeys;
import com.example.demo.utils.ValidationUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import com.example.demo.services.user.IUserService;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/users")
@RequiredArgsConstructor
public class UserController {
	private final IUserService userService;
	private final TokenService tokenService;
	private final LocalizationUtils localizationUtils;


	@PostMapping("/register")
	public ResponseEntity<ResponseObject> createUser(@Valid @RequestBody UserDTO userDTO, BindingResult result) throws Exception {
		if (result.hasErrors()) {
			List<String> errorMessages = result.getFieldErrors()
					.stream()
					.map(FieldError::getDefaultMessage)
					.toList();

			return ResponseEntity.badRequest().body(ResponseObject.builder()
					.status(HttpStatus.BAD_REQUEST)
					.data(null)
					.message(errorMessages.toString())
					.build());
		}
		if (userDTO.getEmail() == null || userDTO.getEmail().trim().isBlank()) {
			if (userDTO.getPhoneNumber() == null || userDTO.getPhoneNumber().isBlank()) {
				return ResponseEntity.badRequest().body(ResponseObject.builder()
						.status(HttpStatus.BAD_REQUEST)
						.data(null)
						.message("Email or Phone number is required")
						.build());
			} else {
				//phone number not blank
				if (!ValidationUtils.isValidPhoneNumber(userDTO.getPhoneNumber())) {
					throw new Exception("Invalid phone number");
				}
			}

		} else {
			//Email not blank
			if (!ValidationUtils.isValidEmail(userDTO.getEmail())) {
				throw new Exception("Invalid email format");
			}
		}
		if (!userDTO.getPassword().equals(userDTO.getRetypePassword())) {
			//registerResponse.setMessage();
			return ResponseEntity.badRequest().body(ResponseObject.builder()
					.status(HttpStatus.BAD_REQUEST)
					.data(null)
					.message(localizationUtils.getLocalizedMessage(MessageKeys.PASSWORD_NOT_MATCH))
					.build());
		}

		User user = userService.createUser(userDTO);
		return ResponseEntity.ok(ResponseObject.builder()
				.status(HttpStatus.CREATED)
				.data(UserResponse.fromUser(user))
				.message("Account registration successful")
				.build());
	}

	@PostMapping("/login")
	public ResponseEntity<ResponseObject> login(@Valid @RequestBody UserLoginDTO userLoginDTO, HttpServletRequest request) throws Exception {
		String token = userService.login(userLoginDTO);
		String userAgent = request.getHeader("User-Agent");
		User userDetail = userService.getUserDetailsFromToken(token);
		Token jwtToken = tokenService.addToken(userDetail, token, isMobileDevice(userAgent));

		LoginResponse loginResponse = LoginResponse.builder()
				//.message(localizationUtils.getLocalizedMessage(MessageKeys.LOGIN_SUCCESSFULLY))
				.token(jwtToken.getToken())
				.tokenType(jwtToken.getTokenType())
				.refreshToken(jwtToken.getRefreshToken())
				.username(userDetail.getUsername())
				.roles(userDetail.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList())
				.id(userDetail.getId())
				.build();

		return ResponseEntity.ok(ResponseObject.builder()
				.status(HttpStatus.OK)
				.data(loginResponse)
				.message("Login successful")
				.build());
	}

	@PostMapping("/refreshToken")
	public ResponseEntity<ResponseObject> refreshToken(@Valid @RequestBody RefreshTokenDTO refreshTokenDTO) throws Exception {
		User userDetail = userService.getUserDetailsFromRefreshToken(refreshTokenDTO.getRefreshToken());
		Token jwtToken = tokenService.refreshToken(refreshTokenDTO.getRefreshToken(), userDetail);
		LoginResponse loginResponse = LoginResponse.builder()
				.message("Refresh token successfully")
				.token(jwtToken.getToken())
				.tokenType(jwtToken.getTokenType())
				.refreshToken(jwtToken.getRefreshToken())
				.username(userDetail.getUsername())
				.roles(userDetail.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList())
				.id(userDetail.getId()).build();
		return ResponseEntity.ok().body(
				ResponseObject.builder()
						.data(loginResponse)
						.message(loginResponse.getMessage())
						.status(HttpStatus.OK)
						.build());
	}

	@PostMapping("/details")
	public ResponseEntity<ResponseObject> getUserDetails(@RequestHeader("Authorization") String authorizationHeader) throws Exception {
		String extractedToken = authorizationHeader.substring(7); // Loại bỏ "Bearer " từ chuỗi token
		User user = userService.getUserDetailsFromToken(extractedToken);
		return ResponseEntity.ok().body(
				ResponseObject.builder()
						.message("Get user's detail successfully")
						.data(UserResponse.fromUser(user))
						.status(HttpStatus.OK)
						.build()
		);
	}

	private boolean isMobileDevice(String userAgent) {
		// Kiểm tra User-Agent header để xác định thiết bị di động
		// Ví dụ đơn giản:
		return userAgent.toLowerCase().contains("mobile");
	}

}
