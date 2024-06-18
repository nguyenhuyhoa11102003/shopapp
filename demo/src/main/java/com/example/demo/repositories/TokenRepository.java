package com.example.demo.repositories;

import com.example.demo.models.Token;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TokenRepository extends JpaRepository<Token, Long> {
	Token findByToken(String token);
}
