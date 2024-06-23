package com.example.demo.services.token;

import com.example.demo.models.Token;
import com.example.demo.models.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
public interface ITokenService {
	Token addToken(User user, String token, boolean isMobileDevice);

	Token refreshToken(String refreshToken, User user) throws Exception;
}

