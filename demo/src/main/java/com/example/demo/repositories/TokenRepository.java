package com.example.demo.repositories;

import com.example.demo.models.Token;
import com.example.demo.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TokenRepository extends JpaRepository<Token, Long> {
	Token findByToken(String token);
	Token findByRefreshToken(String refreshToken);
	List<Token> findByUser(User user);
}
