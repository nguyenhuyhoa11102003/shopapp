package com.example.demo.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import com.example.demo.services.product.IProductRedisService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;


@AllArgsConstructor
@Slf4j
public class ProductListener {
	private final IProductRedisService productRedisService;

	@PrePersist
	public void prePersist(Product product) {log.info("prePersist");
	}

	@PostPersist //save = persis
	public void postPersist(Product product) {
		// Update Redis cache
		log.info("postPersist");
		productRedisService.clear();
	}

	@PreUpdate
	public void preUpdate(Product product) {
		//ApplicationEventPublisher.instance().publishEvent(event);
		log.info("preUpdate");
	}

	@PostUpdate
	public void postUpdate(Product product) {
		// Update Redis cache
		log.info("postUpdate");
		productRedisService.clear();
	}

	@PreRemove
	public void preRemove(Product product) {
		//ApplicationEventPublisher.instance().publishEvent(event);
		log.info("preRemove");
	}

	@PostRemove
	public void postRemove(Product product) {
		// Update Redis cache
		log.info("postRemove");
		productRedisService.clear();
	}


}
