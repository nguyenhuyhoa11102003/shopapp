package com.example.demo.services.token;

import com.example.demo.components.JwtTokenUtils;
import com.example.demo.exceptions.DataNotFoundException;
import com.example.demo.models.Token;
import com.example.demo.models.User;
import com.example.demo.repositories.TokenRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


@Service
@Slf4j
@RequiredArgsConstructor
public class TokenService implements ITokenService {
	private static final int MAX_TOKENS = 3;
	private final TokenRepository tokenRepository;
	private final JwtTokenUtils jwtTokenUtils;

	@Value("${jwt.expiration}")
	private int expiration;

	@Value("${jwt.expiration-refresh-token}")
	private int expirationRefreshToken;


	@Override
	public Token addToken(User user, String token, boolean isMobileDevice) {
		List<Token> userTokens = tokenRepository.findByUser(user);
		int tokenCount = userTokens.size();

		if (tokenCount >= MAX_TOKENS) {
			boolean hasNonMobileToken = !userTokens.stream().allMatch(Token::isMobile);
			Token tokenToDelete;
			if (hasNonMobileToken) {
				tokenToDelete = userTokens.stream()
						.filter(t -> !t.isMobile())
						.findFirst()
						.orElse(userTokens.get(0));
			} else {
				tokenToDelete = userTokens.get(0);
			}
			tokenRepository.delete(tokenToDelete);
		}
		long expirationInSeconds = expiration;
		LocalDateTime expirationDate = LocalDateTime.now().plusSeconds(expirationInSeconds);

		Token newToken = Token.builder()
				.user(user)
				.token(token)
				.revoked(false)
				.expired(false)
				.tokenType("Bearer")
				.expirationDate(expirationDate)
				.isMobile(isMobileDevice)
				.build();

		newToken.setRefreshToken(UUID.randomUUID().toString());
		newToken.setRefreshExpirationDate(LocalDateTime.now().plusSeconds(expirationRefreshToken));
		tokenRepository.save(newToken);
		return newToken;
	}

	@Transactional
	@Override
	public Token refreshToken(String refreshToken, User user) throws Exception {
		Token existingToken = tokenRepository.findByRefreshToken(refreshToken);

		if (existingToken == null) {
			throw new DataNotFoundException("Refresh token does not exist");
		}

		if (existingToken.getRefreshExpirationDate().isBefore(LocalDateTime.now())) {
			tokenRepository.delete(existingToken);
			throw new DataNotFoundException("Refresh token has expired");
		}

		String token = jwtTokenUtils.generateToken(user);
		LocalDateTime expirationDateTime = LocalDateTime.now().plusSeconds(expiration);
		existingToken.setToken(token);
		existingToken.setExpirationDate(expirationDateTime);
		existingToken.setRefreshToken(UUID.randomUUID().toString());
		existingToken.setRefreshExpirationDate(LocalDateTime.now().plusSeconds(expirationRefreshToken));
		return existingToken;
	}
}
