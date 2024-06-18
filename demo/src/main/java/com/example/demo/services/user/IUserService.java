package com.example.demo.services.user;

import com.example.demo.dtos.UpdateUserDTO;
import com.example.demo.dtos.UserDTO;
import com.example.demo.dtos.UserLoginDTO;
import com.example.demo.exceptions.DataNotFoundException;
import com.example.demo.exceptions.InvalidPasswordException;
import com.example.demo.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IUserService {
	User createUser(UserDTO userDTO) throws Exception;

	String login(UserLoginDTO userLoginDT) throws Exception;

	User getUserDetailsFromToken(String token) throws Exception;

	User getUserDetailsFromRefreshToken(String token) throws Exception;

	User updateUser(Long userId, UpdateUserDTO updatedUserDTO) throws Exception;

	Page<User> findAll(String keyword, Pageable pageable) throws Exception;

	void resetPassword(Long userId, String newPassword)
			throws InvalidPasswordException, DataNotFoundException;

	void blockOrEnable(Long userId, Boolean active) throws DataNotFoundException;
    void changeProfileImage(Long userId, String imageName) throws Exception;

}
