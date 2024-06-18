package com.example.demo.services.user.implement;

import com.example.demo.dtos.UpdateUserDTO;
import com.example.demo.dtos.UserDTO;
import com.example.demo.dtos.UserLoginDTO;
import com.example.demo.exceptions.DataNotFoundException;
import com.example.demo.exceptions.InvalidPasswordException;
import com.example.demo.models.User;
import com.example.demo.services.user.IUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


@Service
@Slf4j
@RequiredArgsConstructor
public class UserService implements IUserService {
	@Override
	public User createUser(UserDTO userDTO) throws Exception {
		return null;
	}

	@Override
	public String login(UserLoginDTO userLoginDT) throws Exception {
		return null;
	}

	@Override
	public User getUserDetailsFromToken(String token) throws Exception {
		return null;
	}

	@Override
	public User getUserDetailsFromRefreshToken(String token) throws Exception {
		return null;
	}

	@Override
	public User updateUser(Long userId, UpdateUserDTO updatedUserDTO) throws Exception {
		return null;
	}

	@Override
	public Page<User> findAll(String keyword, Pageable pageable) throws Exception {
		return null;
	}

	@Override
	public void resetPassword(Long userId, String newPassword) throws InvalidPasswordException, DataNotFoundException {

	}

	@Override
	public void blockOrEnable(Long userId, Boolean active) throws DataNotFoundException {

	}

	@Override
	public void changeProfileImage(Long userId, String imageName) throws Exception {

	}
}
