package com.example.demo.services.user.implement;

import com.example.demo.components.LocalizationUtils;
import com.example.demo.dtos.UpdateUserDTO;
import com.example.demo.dtos.UserDTO;
import com.example.demo.dtos.UserLoginDTO;
import com.example.demo.exceptions.DataNotFoundException;
import com.example.demo.exceptions.ExpiredTokenException;
import com.example.demo.exceptions.InvalidPasswordException;
import com.example.demo.exceptions.PermissionDenyException;
import com.example.demo.models.Role;
import com.example.demo.models.Token;
import com.example.demo.models.User;
import com.example.demo.repositories.TokenRepository;
import com.example.demo.repositories.UserRepository;
import com.example.demo.repositories.RoleRepository;
import com.example.demo.components.JwtTokenUtils;

import com.example.demo.services.user.IUserService;
import com.example.demo.utils.MessageKeys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static com.example.demo.utils.ValidationUtils.isValidEmail;


@Service
@Slf4j
@RequiredArgsConstructor
public class UserService implements IUserService {

	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final TokenRepository tokenRepository;
	private final LocalizationUtils localizationUtils;
	private final PasswordEncoder passwordEncoder;
	private final AuthenticationManager authenticationManager;
	private final JwtTokenUtils jwtTokenUtil;

	@Override
	public User createUser(UserDTO userDTO) throws Exception {
		if (!userDTO.getPhoneNumber().isBlank() && userRepository.existsByPhoneNumber(userDTO.getPhoneNumber())) {
			throw new DataIntegrityViolationException("Phone number already exists");
		}
		if (!userDTO.getEmail().isBlank() && userRepository.existsByEmail(userDTO.getEmail())) {
			throw new DataIntegrityViolationException("Email already exists");
		}
		Role role = roleRepository.findById(userDTO.getRoleId())
				.orElseThrow(() -> new DataNotFoundException(
						localizationUtils.getLocalizedMessage(MessageKeys.ROLE_DOES_NOT_EXISTS)));
		if (role.getName().equalsIgnoreCase(Role.ADMIN)) {
			throw new PermissionDenyException("Registering admin accounts is not allowed");
		}

		User newUser = User
				.builder()
				.phoneNumber(userDTO.getPhoneNumber())
				.email(userDTO.getEmail())
				.password(userDTO.getPassword())
				.address(userDTO.getAddress())
				.dateOfBirth(userDTO.getDateOfBirth())
				.facebookAccountId(userDTO.getFacebookAccountId())
				.googleAccountId(userDTO.getGoogleAccountId())
				.active(true)
				.build();

		newUser.setRole(role);

		// Kiểm tra nếu có accountId, không yêu cầu password
		if (userDTO.getFacebookAccountId() == 0 && userDTO.getGoogleAccountId() == 0) {
			String password = userDTO.getPassword();
			String encodedPassword = passwordEncoder.encode(password);
			newUser.setPassword(encodedPassword);
		}
		return userRepository.save(newUser);
	}

	@Override
	public String login(UserLoginDTO userLoginDTO) throws Exception {
		Optional<User> optionalUser = Optional.empty();
		String subject = null;

		// Check if the user exists by phone number
		if (userLoginDTO.getPhoneNumber() != null && !userLoginDTO.getPhoneNumber().isBlank()) {
			optionalUser = userRepository.findByPhoneNumber(userLoginDTO.getPhoneNumber());
			subject = userLoginDTO.getPhoneNumber();
		}

		// If the user is not found by phone number, check by email
		if (optionalUser.isEmpty() && userLoginDTO.getEmail() != null) {
			optionalUser = userRepository.findByEmail(userLoginDTO.getEmail());
			subject = userLoginDTO.getEmail();
		}

		// If user is not found, throw an exception
		if (optionalUser.isEmpty()) {
			throw new DataNotFoundException(localizationUtils.getLocalizedMessage(MessageKeys.WRONG_PHONE_PASSWORD));
		}
		// Get the existing user
		User existingUser = optionalUser.get();
		//check password
		if (existingUser.getFacebookAccountId() == 0
				&& existingUser.getGoogleAccountId() == 0) {
			if (!passwordEncoder.matches(userLoginDTO.getPassword(), existingUser.getPassword())) {
				throw new BadCredentialsException(localizationUtils.getLocalizedMessage(MessageKeys.WRONG_PHONE_PASSWORD));
			}
		}

		if (!existingUser.isActive()) {
			throw new DataNotFoundException(localizationUtils.getLocalizedMessage(MessageKeys.USER_IS_LOCKED));
		}

		UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
				subject, userLoginDTO.getPassword(),
				existingUser.getAuthorities()
		);

		authenticationManager.authenticate(authenticationToken);
		return jwtTokenUtil.generateToken(existingUser);
	}

	@Override
	public User getUserDetailsFromToken(String token) throws Exception {
		if (jwtTokenUtil.isTokenExpired(token)) {
			throw new ExpiredTokenException("Token is expired");
		}
		String subject = jwtTokenUtil.getSubject(token);
		Optional<User> user;
		user = userRepository.findByPhoneNumber(subject);
		if (user.isEmpty() && isValidEmail(subject)) {
			user = userRepository.findByEmail(subject);
		}
		return user.orElseThrow(() -> new Exception("User not found"));
	}

	@Override
	public User getUserDetailsFromRefreshToken(String refreshToken) throws Exception {
		Token existingToken = tokenRepository.findByRefreshToken(refreshToken);
		return getUserDetailsFromToken(existingToken.getToken());
	}

	@Override
	public User updateUser(Long userId, UpdateUserDTO updatedUserDTO) throws Exception {
		// Find the existing user by userId
		User existingUser = userRepository.findById(userId)
				.orElseThrow(() -> new DataNotFoundException("User not found"));
		// Update user information based on the DTO
		if (updatedUserDTO.getFullName() != null) {
			existingUser.setFullName(updatedUserDTO.getFullName());
		}

		if (updatedUserDTO.getAddress() != null) {
			existingUser.setAddress(updatedUserDTO.getAddress());
		}
		if (updatedUserDTO.getDateOfBirth() != null) {
			existingUser.setDateOfBirth(updatedUserDTO.getDateOfBirth());
		}
		if (updatedUserDTO.getFacebookAccountId() > 0) {
			existingUser.setFacebookAccountId(updatedUserDTO.getFacebookAccountId());
		}
		if (updatedUserDTO.getGoogleAccountId() > 0) {
			existingUser.setGoogleAccountId(updatedUserDTO.getGoogleAccountId());
		}

		// Update the password if it is provided in the DTO
		if (updatedUserDTO.getPassword() != null
				&& !updatedUserDTO.getPassword().isEmpty()) {
			if (!updatedUserDTO.getPassword().equals(updatedUserDTO.getRetypePassword())) {
				throw new DataNotFoundException("Password and retype password not the same");
			}
			String newPassword = updatedUserDTO.getPassword();
			String encodedPassword = passwordEncoder.encode(newPassword);
			existingUser.setPassword(encodedPassword);
		}
		return userRepository.save(existingUser);
	}

	@Override
	public Page<User> findAll(String keyword, Pageable pageable) throws Exception {
		return userRepository.findAll(keyword, pageable);
	}

	@Override
	public void resetPassword(Long userId, String newPassword) throws InvalidPasswordException, DataNotFoundException {
		User existingUser = userRepository.findById(userId)
				.orElseThrow(() -> new DataNotFoundException("User not found"));

		String encodedPassword = passwordEncoder.encode(newPassword);
		existingUser.setPassword(encodedPassword);
		userRepository.save(existingUser);
		//reset password => clear token
		List<Token> tokens = tokenRepository.findByUser(existingUser);
		for (Token token : tokens) {
			tokenRepository.delete(token);
		}
	}

	@Override
	public void blockOrEnable(Long userId, Boolean active) throws DataNotFoundException {
		User existingUser = userRepository.findById(userId)
				.orElseThrow(() -> new DataNotFoundException("User not found"));
		existingUser.setActive(active);
		userRepository.save(existingUser);
	}

	@Override
	public void changeProfileImage(Long userId, String imageName) throws Exception {
		User existingUser = userRepository.findById(userId)
				.orElseThrow(() -> new DataNotFoundException("User not found"));
		existingUser.setProfileImage(imageName);
		userRepository.save(existingUser);
	}

	@Override
	public void processOAuthPostLogin(String username) throws DataNotFoundException {
		UserDTO userDTO = UserDTO
				.builder()
				.roleId(1L)
				.build();

		Role role = this.roleRepository.findById(userDTO.getRoleId())
				.orElseThrow(() -> new DataNotFoundException(
						localizationUtils.getLocalizedMessage(MessageKeys.ROLE_DOES_NOT_EXISTS)));

		Optional<User> user = userRepository.findByEmail(username);

		if (user.isEmpty()) {
			User newUser = User
					.builder()
					.email(username)
					.googleAccountId(1)
					.active(true)
					.role(role)
					.build();
			this.userRepository.save(newUser);
		}


	}
}
