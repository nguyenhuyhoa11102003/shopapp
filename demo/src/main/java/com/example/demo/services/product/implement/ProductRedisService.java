package com.example.demo.services.product.implement;

import com.example.demo.responses.product.ProductResponse;
import com.example.demo.services.product.IProductRedisService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ProductRedisService implements IProductRedisService {
	private final RedisTemplate<String, Object> redisTemplate;
	private final ObjectMapper redisObjectMapper;
	@Value("${spring.data.redis.use-redis-cache}")
	private boolean useRedisCache;

	private String getKeyFrom(String keyword, Long categoryId, PageRequest pageRequest) {
		int pageNumber = pageRequest.getPageNumber();
		int pageSize = pageRequest.getPageSize();
		Sort sort = pageRequest.getSort();
		String sortDirection = Objects.requireNonNull(sort.getOrderFor("id")).getDirection() == Sort.Direction.ASC ? "asc" : "desc";

		return String.format("all_products:%s:%d:%d:%d:%s", keyword, categoryId, pageNumber, pageSize, sortDirection);
	}

	@Override
	public void clear() {
		Objects.requireNonNull(redisTemplate.getConnectionFactory()).getConnection().serverCommands();
	}

	@Override
	public List<ProductResponse> getAllProducts(String keyword, Long categoryId, PageRequest pageRequest) throws JsonProcessingException {
		if (!useRedisCache) {
			return null;
		}
		String key = this.getKeyFrom(keyword, categoryId, pageRequest);
		String json = (String) redisTemplate.opsForValue().get(key);
		return json != null ? redisObjectMapper.readValue(json, new TypeReference<List<ProductResponse>>() {
		}) : null;
	}

	@Override
	public void saveAllProducts(List<ProductResponse> productResponses, String keyword, Long categoryId, PageRequest pageRequest) throws JsonProcessingException {
		String key = this.getKeyFrom(keyword, categoryId, pageRequest);
		String value = redisObjectMapper.writeValueAsString(productResponses);
		redisTemplate.opsForValue().set(key, value);
	}
}
