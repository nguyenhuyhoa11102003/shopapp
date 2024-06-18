package com.example.demo.components;

import com.example.demo.exceptions.InvalidParamException;
import com.example.demo.models.User;
import com.example.demo.models.Token;
import com.example.demo.repositories.TokenRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Encoders;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import org.springframework.beans.factory.annotation.Value;

import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.security.SecureRandom;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtTokenUtils {
	@Value("${jwt.expiration}")
	private int expiration;

	@Value("${jwt.expiration-refresh-token}")
	private int expirationRefreshToken;

	@Value("${jwt.secretKey}")
	private String secretKey;

	private final TokenRepository tokenRepository;

	public String generateToken(User user) throws Exception {
		Map<String, Object> claims = new HashMap<>();
		claims.put("id", user.getId());
		String subject = getSubject(user);
		claims.put("subject", subject);
		claims.put("userId", user.getId());
		try {
			return Jwts.builder()
					.setClaims(claims)
					.setSubject(subject)
					.setExpiration(new Date(System.currentTimeMillis() + expiration * 1000L))
					.signWith(getSignInKey(), SignatureAlgorithm.HS256)
					.compact();
		} catch (Exception e) {
			throw new InvalidParamException("Cannot create jwt token, error: " + e.getMessage());
		}
	}

	private Key getSignInKey() {
		byte[] bytes = Decoders.BASE64.decode(secretKey);
		return Keys.hmacShaKeyFor(bytes);
	}

	private static String getSubject(User user) {
		String subject = user.getPhoneNumber();
		if (subject == null || subject.isBlank()) {
			subject = user.getEmail();
		}
		return subject;
	}

	private String generateSecretKey() {
		SecureRandom random = new SecureRandom();
		byte[] keyBytes = new byte[32]; // 256-bit key
		random.nextBytes(keyBytes);
		String secretKey = Encoders.BASE64.encode(keyBytes);
		return secretKey;
	}

	private Claims extractAllClaims(String token) {
		return Jwts.parserBuilder()
				.setSigningKey(getSignInKey())
				.build()
				.parseClaimsJws(token)
				.getBody();
	}

	public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
		final Claims claims = this.extractAllClaims(token);
		return claimsResolver.apply(claims);
	}

	//check expiration
	public boolean isTokenExpired(String token) {
		Date expirationDate = this.extractClaim(token, Claims::getExpiration);
		return expirationDate.before(new Date());
	}

	public String getSubject(String token) {
		return extractClaim(token, Claims::getSubject);
	}

	public boolean validateToken(String token, User userDetails) {
		try {
			String subject = extractClaim(token, Claims::getSubject);
			//subject is phoneNumber or email
			Token existingToken = tokenRepository.findByToken(token);
			if (existingToken == null ||
					existingToken.isRevoked() == true ||
					!userDetails.isActive()
			) {
				return false;
			}
			return (subject.equals(userDetails.getUsername()))
					&& !isTokenExpired(token);
		} catch (MalformedJwtException e) {
			log.error("Invalid JWT token: {}", e.getMessage());
		} catch (ExpiredJwtException e) {
			log.error("JWT token is expired: {}", e.getMessage());
		} catch (UnsupportedJwtException e) {
			log.error("JWT token is unsupported: {}", e.getMessage());
		} catch (IllegalArgumentException e) {
			log.error("JWT claims string is empty: {}", e.getMessage());
		}

		return false;
	}


}
