package com.test.sync.service;

import java.util.NoSuchElementException;
import java.util.Optional;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.test.sycn.redis.dto.ProductStock;
import com.test.sync.entity.Product;
import com.test.sync.repository.ProductRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService {

	private final ProductRepository productRepository;

	private final ProductService productService;
	
  private final RedissonClient redissonClient;

	/**
	 * jpa lock만 사용하는 주문
	 * 
	 * @return
	 */
	@Transactional
	public void orderWithOnlyJPALock(int productId) {
		Product product = productRepository.findById(productId);
		if (product == null)
			throw new NoSuchElementException("Failed to find product.");
		product.decreaseStock();//재고 차감
		//TODO 주문 이력 저장 로직 추가 
	}

	/**
	 * jpa lock과 redis를 사용하는 주문 
	 * @throws JsonProcessingException 
	 */
	@Transactional
	public void orderWithJPALockAndRedis(int productId) throws JsonProcessingException {	
		Optional<ProductStock> stockOpt = productService.findProductStockToRedis(productId);
		ProductStock productStock;
		
		if(stockOpt.isEmpty()) { //redis에 해당 제품의 재고 정보가 없을 경우			
			Product product = productRepository.findById(productId); //rdb에서 제품 정보 조회
			if (product == null)
				throw new NoSuchElementException("Failed to find product.");
			
			int stock = product.getStock();
			productStock = productService.saveProductStockToRedis(productId, stock); //redis에서 조회 가능하도록 rdb에 있는 데이터를 redis에 저장
		}else 
			productStock = stockOpt.get(); //redis에 저장된 재고 정보가 있을 경우	
		
		productStock.decreaseStock();
		productService.saveProductStockToRedis(productId, productStock.getStock());
		//TODO REDIS에 주문 이력 저장 로직 추가
		
		Product product = productRepository.findById(productId);
		if (product == null)
			throw new NoSuchElementException("Failed to find product.");
		product.decreaseStock();

	}

	/**
	 * jpa lock과 redisson lock을 사용하는 주문 
	 * @throws JsonProcessingException 
	 */
	@Transactional
	public void orderWithJPALockAndRedissonLock(int productId) throws JsonProcessingException {	
		//TODO 작성 예정
	}
	
	/**
	 * jpa lock과 lettuce lock을 사용하는 주문 
	 * @throws JsonProcessingException 
	 */
	@Transactional
	public void orderWithJPALockAndLettuceLock(int productId) throws JsonProcessingException {	
		//TODO 작성 예정
	}

}
